# Gradle 設定（Android app module）

在 `com.android.application` module 上接 Checkstyle / SpotBugs / JaCoCo。

核心原因：`checkstyleMain`、`spotbugsMain`、`jacocoTestReport` 這些是 **Java plugin 的 source set 任務**。Android module 用 AGP，不會自動產生它們，直接 `./gradlew checkstyleMain` 會得到 `Task not found`。下面用自訂 task 指向 Android 的 source / class 路徑，讓它們在 Android module 上可用。

以下範例為 Groovy DSL，對應單一 `app` module，加在 `app/build.gradle`。

本文的 **task 寫法與路徑處理**已在真實專案（單一 `app` module）上實機驗證，完整鏈（checkstyle、spotbugs、lintDebug、testDebugUnitTest、jacocoTestReport、assembleDebug）從乾淨狀態 BUILD SUCCESSFUL；此驗證結論與版本號脫鉤，換一組相容的版本組合不影響這些寫法的有效性。

範例中的版本號皆為佔位符（`<...>`）；套用前先依 `references/version-check.md` 推導本專案環境下的當前相容版本，再填入下面對應位置。版本相依規則見 `references/limitations.md`。

## 1. Plugins

在 `app/build.gradle` 既有的 `plugins { }` 區塊加入：

```groovy
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'checkstyle'
    id 'com.github.spotbugs' version '<SPOTBUGS_PLUGIN_VERSION>'
    id 'jacoco'
}
```

## 2. Checkstyle

```groovy
checkstyle {
    toolVersion = '<CHECKSTYLE_VERSION>'
    configFile = rootProject.file('config/checkstyle/checkstyle.xml')
    ignoreFailures = false
}

tasks.register('checkstyle', Checkstyle) {
    source 'src/main/java'
    include '**/*.java'
    classpath = files()
    reports {
        html.required = true
        xml.required = true
    }
}
```

`./gradlew checkstyle` 產出 `app/build/reports/checkstyle/`。

## 3. SpotBugs + find-sec-bugs（SAST）

SpotBugs 分析的是編譯後的 bytecode，所以要指向 javac 的輸出目錄。不要用 glob 去猜 `intermediates` 路徑——AGP 會在 `intermediates/javac/debug` 下同時留 `classes/` 與 `compileDebugJavaWithJavac/classes/` 兩份同名 class，glob 會撈到重複。直接取 `compileDebugJavaWithJavac` 這個編譯 task 的 `destinationDirectory`，永遠是唯一正確路徑，也自動建立 task 依賴。

`ignoreFailures = true` 讓導入既有專案時 task 本身綠燈、報告照產（與 Checkstyle 的 `severity=warning` 一致的軟性門檻）；清乾淨後改 `false` 變硬性門檻。

```groovy
spotbugs {
    toolVersion = '<SPOTBUGS_VERSION>'
    excludeFilter = rootProject.file('config/spotbugs/exclude.xml')
    ignoreFailures = true
}

tasks.register('spotbugs', com.github.spotbugs.snom.SpotBugsTask) {
    dependsOn 'compileDebugJavaWithJavac'
    sourceDirs.setFrom(files('src/main/java'))
    classDirs.setFrom(tasks.named('compileDebugJavaWithJavac', JavaCompile).map { it.destinationDirectory })
    auxClassPaths.setFrom(files())
    reports {
        html.required = true
        xml.required = true
    }
}

dependencies {
    spotbugsPlugins 'com.h3xstream.findsecbugs:findsecbugs-plugin:<FINDSECBUGS_VERSION>'
}
```

`./gradlew spotbugs` 產出 `app/build/reports/spotbugs/`。find-sec-bugs 隨 SpotBugs 一起跑，提供 SAST 的安全規則。

`auxClassPaths` 留空會讓 SpotBugs 對部分第三方 / Android API 回報 missing class 警告，但分析仍能進行；需要更完整分析時再把 `android.jar` 與相依 jar 加進去。

## 4. JaCoCo（覆蓋率）

Android module 不會自動把 `jacocoTestReport` 接到 `testDebugUnitTest`，同樣需自訂 task。套用 `jacoco` plugin 後，`testDebugUnitTest` 會自動在 `build/jacoco/testDebugUnitTest.exec` 產生執行資料，不需要設 `testCoverageEnabled`（那是給 instrumented test 的）。

三個路徑陷阱（都是實機驗證踩出來的）：
- `classDirectories` 同樣用 `compileDebugJavaWithJavac` 的 `destinationDirectory`，避開重複 class。
- `executionData` 只指 `build/jacoco`，**不要掃整個 `build/`**，否則 Gradle 會判定 jacoco 隱性依賴 `build/` 下其他 task 的輸出而報錯。
- `doLast` 解析 JaCoCo XML 必須允許 DOCTYPE（報告含 `<!DOCTYPE>`），否則新版 JDK 的 parser 會擋。

```groovy
jacoco {
    toolVersion = '<JACOCO_VERSION>'
}

tasks.register('jacocoTestReport', JacocoReport) {
    dependsOn 'testDebugUnitTest'
    reports {
        xml.required = true
        html.required = true
    }

    def fileFilter = ['**/R.class', '**/R$*.class', '**/BuildConfig.*', '**/Manifest*.*', '**/databinding/**']
    def javac = tasks.named('compileDebugJavaWithJavac', JavaCompile)

    sourceDirectories.setFrom(files(['src/main/java']))
    classDirectories.setFrom(javac.map { fileTree(it.destinationDirectory.get().asFile).matching { exclude fileFilter } })
    executionData.setFrom(
        fileTree(dir: "${layout.buildDirectory.get().asFile}/jacoco", includes: ['*.exec'])
    )

    doLast {
        def report = file("${layout.buildDirectory.get().asFile}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
        if (report.exists()) {
            def parser = new XmlParser(false, false)
            parser.setFeature('http://apache.org/xml/features/disallow-doctype-decl', false)
            parser.setFeature('http://apache.org/xml/features/nonvalidating/load-external-dtd', false)
            def root = parser.parse(report)
            def counters = root.counter.findAll { it.@type == 'INSTRUCTION' }
            def missed = counters.sum { it.@missed.toInteger() } ?: 0
            def covered = counters.sum { it.@covered.toInteger() } ?: 0
            def total = missed + covered
            def pct = total ? (covered * 100 / total) as int : 0
            println "Total coverage: ${pct}%"
        }
    }
}
```

`./gradlew testDebugUnitTest jacocoTestReport` 產出 `app/build/reports/jacoco/` 並在 log 印出覆蓋率百分比。

## 首次驗證

加完設定後，務必在本機跑一次，確認每個 task 都存在且能完成：

```
./gradlew checkstyle spotbugs lintDebug testDebugUnitTest jacocoTestReport assembleDebug
```

若 `spotbugs` 或 `jacocoTestReport` 找不到 class / exec，多半是 AGP 的 `intermediates` 路徑與範例不同，依 `references/troubleshooting.md` 調整 glob。
