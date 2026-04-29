---
name: android-ci-pipeline
description: Use when setting up, modifying, or troubleshooting a CI pipeline for an Android Java/Kotlin project on GitLab (gitlab.com or self-hosted). Triggers on "setup CI", "add GitLab CI", "create pipeline", "configure CI", "automate build", "add lint check", "add unit test stage", "Checkstyle", "SpotBugs", "ktlint", "detekt", "build APK in CI", "pre-commit hook", "merge request pipeline", or any request involving continuous integration for Android on GitLab. Does NOT cover CD (deploy / release distribution).
version: 3.0.0
---

# Android CI Pipeline（GitLab CI，Java 主、Kotlin 兼容）

此 skill 適用於 **Android Studio** 開發、**Java 為主（混 Kotlin 亦可）**、推送至 **GitLab（gitlab.com 或 self-hosted）** 的專案，建立 **CI（持續整合）** pipeline。**不包含 CD**（部署 / Firebase App Distribution / Play Store 上架等）。

吸收測試金字塔、本地守門員、雙層分支合併等實務做法。

---

## 一、整體架構

```
[Android Studio 本地]            [遠端 GitLab]
pre-commit (git hook) ─push─►   feature branch
   │                                │  (Merge Request)
   ├─ Checkstyle / ktlint           ▼
   ├─ SpotBugs / detekt          MR Pipeline ──► develop branch
   └─ lintDebug                     │  (Merge Request)
                                    ▼
                                MR Pipeline ──► main branch
```

### 分支策略（feature → develop → main）

| 分支 | 用途 | 觸發 pipeline |
|---|---|---|
| `feature/*` | 開發分支 | 推送觸發 lint + unit test（輕量）|
| `develop` | 整合分支 | MR 合併觸發完整 pipeline |
| `main` | 發佈分支 | MR 合併觸發完整 pipeline + 簽名 build artifact |

> **GitLab 設定**：Settings → Repository → Protected branches，將 `main` 與 `develop` 設為 protected；Settings → Merge requests 啟用「Pipelines must succeed」。

---

## 二、Pipeline 階段（5 stages，對應測試金字塔）

```
pre-check → lint → test → security → build
```

| Stage | Java 工具 | Kotlin 工具 | Gradle 指令 | 對應 hackmd 概念 |
|---|---|---|---|---|
| pre-check | **Checkstyle** | ktlint | `./gradlew checkstyleMain ktlintCheck` | 風格檢查（ruff）|
| lint | **SpotBugs + PMD** + Android Lint | detekt + Android Lint | `./gradlew spotbugsMain pmdMain detekt lintDebug` | 型別 / 靜態分析（mypy）|
| test | JUnit + Mockito + Jacoco | JUnit + MockK + Jacoco | `./gradlew testDebugUnitTest jacocoTestReport` | Unit + Robolectric（unit / integration）|
| security | GitLab SAST + Dependency Scanning + Secret Detection | （同左）| 內建 template | 安全防線 |
| build | Gradle | Gradle | `./gradlew assembleDebug` / `assembleRelease` | Build artifact |

> **Java vs Kotlin 工具對照**
> - Style：Checkstyle ↔ ktlint（功能對等，依語言選用，混合專案兩個都跑）
> - Static analysis：SpotBugs（含 find-sec-bugs）+ PMD ↔ detekt
> - Android Lint 兩邊通用，必跑

> **Robolectric / Instrumentation Test**
> - **Robolectric**（JVM 上跑的 Android 測試）已隨 `testDebugUnitTest` 跑，不需獨立 stage。
> - **Instrumentation（Espresso）**：gitlab.com 共用 runner 無 KVM 無法跑 emulator。若需要：（a）**自架 GitLab Runner** 開啟 KVM 跑 emulator，或（b）將 APK 上傳至 Firebase Test Lab（屬本 skill 範圍外）。

---

## 三、本地守門員：Pre-commit Hook（Android Studio 友善版）

### 3.1 Git Hook（推薦，無需額外工具）

`.git/hooks/pre-commit`（記得 `chmod +x .git/hooks/pre-commit`）：

```bash
#!/bin/sh
set -e
echo "[pre-commit] running style + static checks..."
./gradlew checkstyleMain ktlintCheck spotbugsMain pmdMain detekt --daemon
```

`.git/hooks/pre-push`：

```bash
#!/bin/sh
set -e
echo "[pre-push] running lint + unit tests..."
./gradlew lintDebug testDebugUnitTest --daemon
```

> **為什麼放 git hook 而非 pre-commit 框架？** Android Studio 開發者多半沒裝 Python/pip；git hook 是零依賴方案，且 Gradle daemon 有 cache，速度尚可接受。

### 3.2 進階：pre-commit 框架（選用，需 Python）

若團隊已用 Python 工具，可改用 `.pre-commit-config.yaml`：

```yaml
repos:
  - repo: local
    hooks:
      - id: style
        name: style (checkstyle/ktlint)
        entry: ./gradlew checkstyleMain ktlintCheck
        language: system
        pass_filenames: false
        stages: [commit]
      - id: static
        name: static (spotbugs/pmd/detekt)
        entry: ./gradlew spotbugsMain pmdMain detekt
        language: system
        pass_filenames: false
        stages: [commit]
      - id: tests
        name: lint + unit test
        entry: ./gradlew lintDebug testDebugUnitTest
        language: system
        pass_filenames: false
        stages: [push]
```

啟用：`pip install pre-commit && pre-commit install && pre-commit install --hook-type pre-push`

### 3.3 把 hook 共享給團隊

`.git/hooks/` 不會被 git 追蹤。建議把 hook 放到 `scripts/git-hooks/`，並在 `build.gradle`（root）加：

```groovy
tasks.register("installGitHooks", Copy) {
    from("$rootDir/scripts/git-hooks/")
    into("$rootDir/.git/hooks/")
    fileMode = 0755
}
// 讓任何 build 都會先安裝 hook
tasks.matching { it.name == "preBuild" }.configureEach { dependsOn("installGitHooks") }
```

---

## 四、Android Studio 整合

讓 IDE 內檢查與 CI 規則一致，避免「本地過 / CI 掛」。

### 4.1 IDE 外掛（建議全裝）

| 外掛 | 用途 |
|---|---|
| **CheckStyle-IDEA** | 即時 Checkstyle 檢查 |
| **SpotBugs IDEA** | 即時 SpotBugs 檢查 |
| **PMDPlugin** | 即時 PMD 檢查 |
| **ktlint** / **detekt** | Kotlin 端對等檢查 |
| **GitToolBox** | git hook 狀態提示 |

### 4.2 規則檔集中管理

```
<repo-root>/
├── config/
│   ├── checkstyle/checkstyle.xml      # IDE 與 Gradle 共用
│   ├── spotbugs/exclude.xml
│   ├── pmd/ruleset.xml
│   └── detekt/detekt.yml
└── .editorconfig                       # IDE 自動套用 + ktlint 讀取
```

於 IDE：Settings → Tools → Checkstyle → 指向 `config/checkstyle/checkstyle.xml`，其餘外掛同理。**團隊使用同一份檔案**，CI 與 IDE 完全一致。

### 4.3 Code Style 設定

- Settings → Editor → Code Style → Java → 從 `config/checkstyle/checkstyle.xml` import scheme
- 啟用 `.editorconfig` support（預設已開）
- 開啟「Reformat on save」+「Optimize imports on save」前，先確認規則與 Checkstyle/ktlint 一致，否則會出現 IDE 改完反而 CI 掛的窘境

### 4.4 Gradle Tasks 面板

直接從 AS 右側 Gradle 面板雙擊執行，與 CI 完全相同：
- `verification → check`（跑全部 lint + test）
- `verification → testDebugUnitTest`
- `other → checkstyleMain` / `spotbugsMain` / `pmdMain`

---

## 五、`.gitlab-ci.yml` 完整範本

放在 repo 根目錄：

```yaml
# ════════════════════════════════════════════════════════════
# Android CI on GitLab（Java 主，Kotlin 兼容）
# 不含 CD：CI only — lint / test / security / build artifact
# ════════════════════════════════════════════════════════════

# 推薦 image（擇一）：
#   - mingc/android-build-box:latest    （社群活躍，含 JDK 17 + SDK）
#   - jangrewe/gitlab-ci-android:latest （GitLab 社群常用）
#   - eclipse-temurin:17-jdk + 自行安裝 cmdline-tools（最穩，自控版本）
# ⚠️ 正式環境請 pin digest（`@sha256:...`），勿用 `latest`
image: mingc/android-build-box:latest

# ── 全域變數 ─────────────────────────────────────────────
variables:
  GRADLE_OPTS: "-Dorg.gradle.daemon=false -Dorg.gradle.caching=true -Dorg.gradle.parallel=true"
  GRADLE_USER_HOME: "$CI_PROJECT_DIR/.gradle"   # 必須在專案內，cache 才抓得到
  ANDROID_COMPILE_SDK: "34"

# ── Pipeline 觸發規則（feature → develop → main）─────────
workflow:
  rules:
    - if: $CI_PIPELINE_SOURCE == "merge_request_event"
    - if: $CI_COMMIT_BRANCH == "main" && $CI_PIPELINE_SOURCE != "merge_request_event"
    - if: $CI_COMMIT_BRANCH == "develop" && $CI_PIPELINE_SOURCE != "merge_request_event"
    - if: $CI_COMMIT_BRANCH =~ /^feature\/.*/ && $CI_PIPELINE_SOURCE != "merge_request_event"

# ── Stages ───────────────────────────────────────────────
stages:
  - pre-check
  - lint
  - test
  - security
  - build

# ── 共用 cache（YAML anchor）─────────────────────────────
.gradle-cache: &gradle-cache
  cache:
    key:
      files:
        - gradle/wrapper/gradle-wrapper.properties
        - "**/build.gradle*"
    paths:
      - .gradle/caches
      - .gradle/wrapper
      - .gradle/build-cache
    policy: pull-push

.before-script: &before-script
  before_script:
    - chmod +x ./gradlew
    - export GRADLE_USER_HOME=$CI_PROJECT_DIR/.gradle

# ════════ Stage 1: pre-check（風格） ═══════════════════════
# Java 與 Kotlin 兩個 job 平行跑，DAG 加速
checkstyle:
  stage: pre-check
  <<: *gradle-cache
  <<: *before-script
  script:
    - ./gradlew checkstyleMain
  artifacts:
    when: always
    paths:
      - app/build/reports/checkstyle/
    expire_in: 1 week
  rules:
    - exists:
        - config/checkstyle/checkstyle.xml

ktlint:
  stage: pre-check
  <<: *gradle-cache
  <<: *before-script
  script:
    - ./gradlew ktlintCheck
  artifacts:
    when: on_failure
    paths:
      - app/build/reports/ktlint/
  rules:
    - exists:
        - "**/*.kt"

# ════════ Stage 2: lint（靜態分析） ═══════════════════════
# 三個 job 平行跑（needs: []），不必等 pre-check 全部完成
spotbugs:
  stage: lint
  needs: []
  <<: *gradle-cache
  <<: *before-script
  script:
    - ./gradlew spotbugsMain pmdMain
  artifacts:
    when: always
    paths:
      - app/build/reports/spotbugs/
      - app/build/reports/pmd/
    expire_in: 1 week

detekt:
  stage: lint
  needs: []
  <<: *gradle-cache
  <<: *before-script
  script:
    - ./gradlew detekt
  artifacts:
    when: always
    paths:
      - app/build/reports/detekt/
    expire_in: 1 week
  rules:
    - exists:
        - "**/*.kt"

android-lint:
  stage: lint
  needs: []
  <<: *gradle-cache
  <<: *before-script
  script:
    - ./gradlew lintDebug
  artifacts:
    when: always
    paths:
      - app/build/reports/lint-results-debug.html
      - app/build/reports/lint-results-debug.xml
    expire_in: 1 week

# ════════ Stage 3: test（含 Robolectric） ═════════════════
unit-test:
  stage: test
  needs: [checkstyle, spotbugs, android-lint]
  <<: *gradle-cache
  <<: *before-script
  script:
    - ./gradlew testDebugUnitTest jacocoTestReport
  coverage: '/Total.*?([0-9]{1,3})%/'
  artifacts:
    when: always
    reports:
      junit: app/build/test-results/testDebugUnitTest/TEST-*.xml
      coverage_report:
        coverage_format: jacoco
        path: app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml
    paths:
      - app/build/reports/tests/
      - app/build/reports/jacoco/
    expire_in: 1 week

# ════════ Stage 4: security（GitLab 內建） ════════════════
include:
  - template: Security/SAST.gitlab-ci.yml
  - template: Security/Dependency-Scanning.gitlab-ci.yml
  - template: Security/Secret-Detection.gitlab-ci.yml

sast:
  stage: security
  needs: []

dependency_scanning:
  stage: security
  needs: []

secret_detection:
  stage: security
  needs: []

# ════════ Stage 5: build ════════════════════════════════
build-debug:
  stage: build
  needs: [unit-test]
  <<: *gradle-cache
  <<: *before-script
  script:
    - ./gradlew assembleDebug --stacktrace
  artifacts:
    name: "debug-apk-$CI_COMMIT_SHORT_SHA"
    paths:
      - app/build/outputs/apk/debug/app-debug.apk
    expire_in: 1 month
  rules:
    - if: $CI_COMMIT_BRANCH != "main"

build-release:
  stage: build
  needs: [unit-test, sast, dependency_scanning]
  <<: *gradle-cache
  <<: *before-script
  script:
    # 從 GitLab CI Variables 還原 keystore（簽名後產出 artifact 即止；不上架 = 非 CD）
    - echo "$KEYSTORE_BASE64" | base64 -d > app/release.keystore
    - ./gradlew assembleRelease
        -Pandroid.injected.signing.store.file=$CI_PROJECT_DIR/app/release.keystore
        -Pandroid.injected.signing.store.password=$STORE_PASSWORD
        -Pandroid.injected.signing.key.alias=$KEY_ALIAS
        -Pandroid.injected.signing.key.password=$KEY_PASSWORD
  artifacts:
    name: "release-apk-$CI_COMMIT_SHORT_SHA"
    paths:
      - app/build/outputs/apk/release/app-release.apk
    expire_in: 6 months
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
```

---

## 六、GitLab CI Variables（Settings → CI/CD → Variables）

僅保留 CI 必需的簽名變數（CD 相關全數移除）：

| Variable Key | Type | Protected | Masked | 用途 |
|---|---|---|---|---|
| `KEYSTORE_BASE64` | Variable | ✅ | ✅ | release 簽名（用 `base64 keystore.jks` 取得） |
| `STORE_PASSWORD` | Variable | ✅ | ✅ | keystore 密碼 |
| `KEY_ALIAS` | Variable | ✅ | ✅ | key alias |
| `KEY_PASSWORD` | Variable | ✅ | ✅ | key 密碼 |

> **Protected** 表示僅 protected branch（main / develop）可存取，避免 feature branch 洩漏簽名金鑰。

---

## 七、Merge Request 守門設定

GitLab → Settings → Merge requests，啟用：

- ✅ **Pipelines must succeed**：pipeline 失敗無法 merge
- ✅ **All threads must be resolved**：所有 review 留言必須解決
- ✅ **Squash commits when merging**：保持 main 歷史乾淨
- ✅ **Delete source branch**：合併後自動刪除 feature

GitLab → Settings → Repository → Protected branches：

- `main` / `develop`：Allowed to push = No one；Allowed to merge = Maintainers
- `main`：Code owner approval required ✅

---

## 八、Test Pyramid 對應（hackmd → Android）

| hackmd（Python） | Android（Java 主 / Kotlin 兼容） | Stage |
|---|---|---|
| ruff（風格） | **Checkstyle** / ktlint | pre-check |
| mypy（型別） | **SpotBugs + PMD** / detekt + Android Lint | lint |
| pytest unit | JUnit + **Mockito** / MockK | test |
| pytest integration | Robolectric（隨 unit test 跑） | test |
| Playwright E2E | Espresso（自架 runner 或 Firebase Test Lab，**本 skill 不涵蓋**）| — |
| docker build | assembleDebug / assembleRelease | build |

---

## 九、常見問題

| 問題 | 原因 | 解法 |
|---|---|---|
| Cache 每次都 miss | `GRADLE_USER_HOME` 沒設在專案內 | 設 `GRADLE_USER_HOME=$CI_PROJECT_DIR/.gradle` |
| `Permission denied: ./gradlew` | gradlew 未設執行權限 | 加 `chmod +x ./gradlew` |
| 想跑 emulator / Espresso | gitlab.com 共用 runner 無 KVM | 自架 Runner 開 KVM，或接 Firebase Test Lab（CD 範圍）|
| Coverage 不顯示在 MR | regex 不對 | `coverage: '/Total.*?([0-9]{1,3})%/'` 並啟用 jacoco report |
| Release secrets 在 feature branch 跑 | Variable 未設 Protected | Variable 改 Protected，job rule 限定 main |
| MR pipeline 跑兩次 | branch + MR 同時觸發 | `workflow:rules:` 中對 branch rule 加 `&& $CI_PIPELINE_SOURCE != "merge_request_event"` |
| Configuration cache 報錯 | AGP 舊版或 plugin 不相容 | `gradle.properties` 拿掉 `org.gradle.configuration-cache=true`，先用 build cache |
| SpotBugs 報太多 false positive | 規則太嚴 | 在 `config/spotbugs/exclude.xml` 加排除；改用 find-sec-bugs 子集 |
| Checkstyle 與 IDE 自動格式化打架 | IDE Code Style 與 checkstyle.xml 不一致 | 從 checkstyle.xml import 成 IDE Code Scheme |

---

## 十、`gradle.properties` 推薦設定

```properties
org.gradle.jvmargs=-Xmx4096m -XX:+UseParallelGC
org.gradle.parallel=true
org.gradle.caching=true
# org.gradle.configuration-cache=true   # AGP 8.0+ 才開；遇 plugin 不相容請註解
android.useAndroidX=true
android.enableJetifier=false
kotlin.incremental=true
```

---

## 十一、Java 端 Gradle 設定範例（root `build.gradle`）

```groovy
subprojects {
    apply plugin: 'checkstyle'
    apply plugin: 'pmd'
    apply plugin: 'com.github.spotbugs'

    checkstyle {
        toolVersion = '10.17.0'
        configFile = rootProject.file('config/checkstyle/checkstyle.xml')
    }
    pmd {
        toolVersion = '7.0.0'
        ruleSetFiles = files(rootProject.file('config/pmd/ruleset.xml'))
        ruleSets = []   // 用自訂 ruleset
    }
    spotbugs {
        excludeFilter = rootProject.file('config/spotbugs/exclude.xml')
        // 加 find-sec-bugs：security 強化
    }
    dependencies {
        spotbugsPlugins 'com.h3xstream.findsecbugs:findsecbugs-plugin:1.13.0'
    }
}
```

> 參考：[SpotBugs Gradle Plugin](https://github.com/spotbugs/spotbugs-gradle-plugin) / [GitLab Code Quality 整合](https://github.com/chkal/gitlab-code-quality-plugin)（可把 Checkstyle/SpotBugs XML 轉成 GitLab Code Quality JSON，於 MR widget 顯示）

---

## 十二、檢查清單

設定或修改 CI pipeline 後，確認：

### Android Studio 端
- [ ] Checkstyle-IDEA / SpotBugs IDEA / PMDPlugin 已安裝並指向 `config/` 規則檔
- [ ] Code Style 已從 `checkstyle.xml` import
- [ ] `.editorconfig` 存在於 repo 根目錄
- [ ] Gradle 面板可直接執行 `check` / `testDebugUnitTest`

### 本地守門員
- [ ] `scripts/git-hooks/pre-commit` 與 `pre-push` 已建立
- [ ] `installGitHooks` Gradle task 已串到 `preBuild`
- [ ] 本地 commit 會跑 Checkstyle + SpotBugs + PMD（+ ktlint/detekt 若有 Kotlin）
- [ ] 本地 push 會跑 lintDebug + unit test

### GitLab CI 設定
- [ ] `.gitlab-ci.yml` 已建立於 repo 根目錄
- [ ] Docker image 已 pin 版本（不用 `latest`）
- [ ] `GRADLE_USER_HOME` 設於 `$CI_PROJECT_DIR/.gradle`
- [ ] cache key 包含 `gradle-wrapper.properties` 與 `**/build.gradle*`
- [ ] 每個 job 都有 `chmod +x ./gradlew`
- [ ] `workflow:rules:` 已限制觸發條件，避免 pipeline 跑兩次
- [ ] 5 stages 透過 `needs:` 正確串聯（DAG，可平行）
- [ ] Jacoco coverage report 路徑正確
- [ ] JUnit reports 路徑正確（MR 頁面顯示）
- [ ] 已 include GitLab SAST / Dependency Scanning / Secret Detection 三套 template

### Java / Kotlin 工具鏈
- [ ] root `build.gradle` 已 apply checkstyle / pmd / spotbugs
- [ ] `config/checkstyle/checkstyle.xml` 等規則檔存在
- [ ] 若有 Kotlin：ktlint + detekt plugin 已 apply
- [ ] Android Lint baseline 已建立（避免歷史包袱阻擋）

### Secrets
- [ ] 所有簽名相關 variable 設為 **Protected + Masked**
- [ ] `KEYSTORE_BASE64` 用 base64 格式儲存
- [ ] **無** CD 相關 variables（Firebase / Play Store token 等）

### 分支保護
- [ ] `main` / `develop` 設為 Protected branch
- [ ] MR 啟用「Pipelines must succeed」
- [ ] MR 啟用「All threads must be resolved」
- [ ] release build job 限定 `$CI_COMMIT_BRANCH == "main"`

### Test Pyramid
- [ ] Checkstyle/ktlint（style）→ SpotBugs+PMD/detekt（static）→ JUnit + Robolectric（unit/integration）皆有對應 stage
- [ ] feature branch 跑輕量 pipeline（lint + unit test）
- [ ] develop / main 跑完整 pipeline（含 security + signed build）
- [ ] **無 CD job**（無 deploy / 上架 / Firebase App Distribution）
