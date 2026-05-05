---
name: android-ci-pipeline
description: Use when setting up, modifying, or troubleshooting a CI pipeline for an Android Java/Kotlin project on GitLab (gitlab.com or self-hosted). Triggers on Chinese or English "設定 GitLab CI / 建立 pipeline / 加入 lint stage / 新增 unit test 階段 / 設定 pre-commit hook / MR pipeline / build APK / 簽名打包 / Checkstyle / SpotBugs / PMD / ktlint / detekt", "setup CI", "add GitLab CI", "create pipeline", "configure CI", "automate build", "add lint check", "add unit test stage", "Checkstyle", "SpotBugs", "ktlint", "detekt", "build APK in CI", "pre-commit hook", "merge request pipeline", or any request involving continuous integration for Android on GitLab. Does NOT cover CD (deploy / release distribution / Firebase App Distribution / Play Store upload).
version: 3.1.0
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
| **Save Actions** / built-in *Reformat on save* | 存檔自動套規則（避免手動觸發 lint）|

> 註：GitToolBox 是 git 狀態列顯示外掛，**不負責 git hook 執行**；hook 是由 git 本身執行 `.git/hooks/`，IDE 不需特別整合。

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
#   - mingc/android-build-box:1.24.0      （社群活躍，含 JDK 17 + SDK；可直接使用）
#   - jangrewe/gitlab-ci-android:2024.06   （GitLab 社群常用）
#   - eclipse-temurin:17-jdk + 自行安裝 cmdline-tools（最穩，自控版本）
#
# ⚠️ 正式環境建議鎖定 digest（防止上游推 tag 覆蓋）。取得 digest 的方式：
#     docker pull mingc/android-build-box:1.24.0
#     docker inspect --format='{{index .RepoDigests 0}}' mingc/android-build-box:1.24.0
#     # 取得後改為： mingc/android-build-box:1.24.0@sha256:abcd1234...
# 直接複製本範本可運作（用 tag）；上線前再補 digest，不要照抄 `<digest>` 佔位符。
image: mingc/android-build-box:1.24.0

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
# 拆成 rw / ro 避免多 job 平行時互相覆蓋寫 cache：
#   - 第一個跑的 job 用 rw (pull-push) 建 cache
#   - 後續平行 job 全部 ro (pull) 只讀，不寫
.gradle-cache-rw: &gradle-cache-rw
  cache:
    key:
      files:
        - gradle/wrapper/gradle-wrapper.properties
        - "**/build.gradle*"
        - "**/build.gradle.kts"
    paths:
      - .gradle/caches
      - .gradle/wrapper
      - .gradle/build-cache
    policy: pull-push

.gradle-cache-ro: &gradle-cache-ro
  cache:
    key:
      files:
        - gradle/wrapper/gradle-wrapper.properties
        - "**/build.gradle*"
        - "**/build.gradle.kts"
    paths:
      - .gradle/caches
      - .gradle/wrapper
      - .gradle/build-cache
    policy: pull

.before-script: &before-script
  before_script:
    - chmod +x ./gradlew
    - export GRADLE_USER_HOME=$CI_PROJECT_DIR/.gradle

# ── Job 觸發 rules anchor（feature 跑輕量，develop/main 跑完整）──
# feature 分支：只跑 lint + test（不跑 security / signed build）
# develop / main / MR：完整 pipeline
.rules-all: &rules-all
  rules:
    - if: $CI_PIPELINE_SOURCE == "merge_request_event"
    - if: $CI_COMMIT_BRANCH == "main"
    - if: $CI_COMMIT_BRANCH == "develop"
    - if: $CI_COMMIT_BRANCH =~ /^feature\/.*/

.rules-integration-only: &rules-integration-only
  rules:
    - if: $CI_PIPELINE_SOURCE == "merge_request_event"
    - if: $CI_COMMIT_BRANCH == "main"
    - if: $CI_COMMIT_BRANCH == "develop"

# ════════ Stage 1: pre-check（風格） ═══════════════════════
# checkstyle 是 pipeline 第一個被排程的 job，由它建立 cache
checkstyle:
  stage: pre-check
  <<: *gradle-cache-rw     # 第一個 job：建立並寫入 cache
  <<: *before-script
  script:
    - ./gradlew checkstyleMain
  artifacts:
    when: always
    paths:
      - "**/build/reports/checkstyle/"   # 多 module 通用
    expire_in: 1 week
  rules:
    - exists:
        - config/checkstyle/checkstyle.xml

ktlint:
  stage: pre-check
  needs: [checkstyle]                    # 等 cache 建好才 pull
  <<: *gradle-cache-ro
  <<: *before-script
  script:
    - ./gradlew ktlintCheck
  artifacts:
    when: on_failure
    paths:
      - "**/build/reports/ktlint/"
  rules:
    - exists:
        - "**/*.kt"

# ════════ Stage 2: lint（靜態分析） ═══════════════════════
# 三個 job 平行跑（needs: [checkstyle] 只是為了 cache，DAG 仍可平行）
static-analysis:                          # 改名：原本叫 spotbugs 但實際跑 spotbugs + pmd
  stage: lint
  needs:
    - job: checkstyle
      optional: true                      # checkstyle 可能因 rules 被 skip，下游不要連帶失敗
  <<: *gradle-cache-ro
  <<: *before-script
  script:
    - ./gradlew spotbugsMain pmdMain
  artifacts:
    when: always
    paths:
      - "**/build/reports/spotbugs/"
      - "**/build/reports/pmd/"
    expire_in: 1 week

detekt:
  stage: lint
  needs:
    - job: checkstyle
      optional: true
  <<: *gradle-cache-ro
  <<: *before-script
  script:
    - ./gradlew detekt
  artifacts:
    when: always
    paths:
      - "**/build/reports/detekt/"
    expire_in: 1 week
  rules:
    - exists:
        - "**/*.kt"

android-lint:
  stage: lint
  needs:
    - job: checkstyle
      optional: true
  <<: *gradle-cache-ro
  <<: *before-script
  script:
    - ./gradlew lintDebug
  artifacts:
    when: always
    paths:
      - "**/build/reports/lint-results-debug.html"
      - "**/build/reports/lint-results-debug.xml"
    expire_in: 1 week

# ════════ Stage 3: test（含 Robolectric） ═════════════════
unit-test:
  stage: test
  needs:
    - job: checkstyle
      optional: true
    - job: static-analysis
    - job: android-lint
  <<: *gradle-cache-ro
  <<: *before-script
  script:
    - ./gradlew testDebugUnitTest jacocoTestReport
    # 將 jacoco XML 中的 coverage 印到 stdout，讓 GitLab 的 coverage regex 抓得到
    # 註：jacocoTestReport 預設不印百分比，必須手動 parse XML，否則 MR 看不到 coverage
    - |
      python3 -c "
      import xml.etree.ElementTree as ET, glob, sys
      total_missed = total_covered = 0
      for f in glob.glob('**/build/reports/jacoco/**/jacocoTestReport.xml', recursive=True):
          tree = ET.parse(f)
          for c in tree.getroot().findall('counter'):
              if c.get('type') == 'INSTRUCTION':
                  total_missed += int(c.get('missed', 0))
                  total_covered += int(c.get('covered', 0))
      total = total_missed + total_covered
      pct = (total_covered * 100 // total) if total else 0
      print(f'Total coverage: {pct}%')
      "
  coverage: '/Total coverage: ([0-9]{1,3})%/'
  artifacts:
    when: always
    reports:
      junit: "**/build/test-results/testDebugUnitTest/TEST-*.xml"
      coverage_report:
        coverage_format: jacoco
        path: "**/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"
    paths:
      - "**/build/reports/tests/"
      - "**/build/reports/jacoco/"
    expire_in: 1 week

# ════════ Stage 4: security（GitLab 內建，僅 develop / main / MR）═
# 注意：SAST + Dependency Scanning 部分功能需 GitLab Premium / Ultimate；
#      Free tier 僅 Secret Detection 完整可用，其餘會降級或 skip。
include:
  - template: Security/SAST.gitlab-ci.yml
  - template: Security/Dependency-Scanning.gitlab-ci.yml
  - template: Security/Secret-Detection.gitlab-ci.yml

sast:
  stage: security
  needs: []
  <<: *rules-integration-only            # feature branch 不跑

dependency_scanning:
  stage: security
  needs: []
  <<: *rules-integration-only

secret_detection:
  stage: security
  needs: []
  <<: *rules-integration-only

# ════════ Stage 5: build ════════════════════════════════
# feature / develop / MR → debug APK
# main → signed release APK
build-debug:
  stage: build
  needs: [unit-test]
  <<: *gradle-cache-ro
  <<: *before-script
  script:
    - ./gradlew assembleDebug --stacktrace
  artifacts:
    name: "debug-apk-$CI_COMMIT_SHORT_SHA"
    paths:
      - "**/build/outputs/apk/debug/*-debug.apk"
    expire_in: 1 month
  rules:
    # main 走 release，不重複出 debug
    - if: $CI_COMMIT_BRANCH == "main"
      when: never
    - if: $CI_PIPELINE_SOURCE == "merge_request_event"
    - if: $CI_COMMIT_BRANCH == "develop"
    - if: $CI_COMMIT_BRANCH =~ /^feature\/.*/

build-release:
  stage: build
  needs: [unit-test, sast, dependency_scanning]
  <<: *gradle-cache-ro
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
      - "**/build/outputs/apk/release/*-release.apk"
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
- ❌ **Squash commits when merging**：**不要啟用**（與 git_skill 紅線「禁止 squash merge」一致，保持線性歷史與作者歸屬）
- ✅ **Delete source branch**：合併後自動刪除 feature（與 git_skill cleanup 流程一致）

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

## 十二、⚠️ 適用前提與已知限制

本範本以「典型單一 app module、Java 為主、Groovy DSL」的專案為基礎設計。複製到其他人的專案時請先比對以下假設：

### 12.1 專案結構假設
- **單一 `app/` module**：原始 artifact 路徑 hardcode `app/build/...`，本版已改為 `**/build/...` 通用 glob，多 module 專案可直接使用，但 **artifact 數量會增加**，留意 GitLab artifact size limit。
- **若你的專案 module 名不是 `app/`**（例如 `mobile/`、`presentation/`），不需改 yaml，但 §三 pre-commit hook 與 §十一 範例文字描述要替換。

### 12.2 Gradle DSL
- §十一 root `build.gradle` 範例為 **Groovy DSL**。AGP 8+ 新建專案預設 **Kotlin DSL（`build.gradle.kts`）**，需轉換語法：
  - `apply plugin: 'checkstyle'` → `plugins { checkstyle }` 或 `apply(plugin = "checkstyle")`
  - `rootProject.file('...')` → `rootProject.file("...")`（雙引號）
  - `fileMode = 0755` → `filePermissions { unix("0755") }`（Gradle 8.3+）

### 12.3 SpotBugs / Checkstyle / PMD 與 Android module
- `spotbugsMain` / `checkstyleMain` / `pmdMain` 是 **Java plugin** 的 source set 任務名。**Android application/library module 不會自動產生這些任務**（AGP 用 `Debug` / `Release` source set）。
- 解法擇一：
  1. **只在 library / pure-Java module 跑這些工具**（`subprojects { if (project.plugins.hasPlugin('java')) { ... } }`）
  2. **為 Android module 自訂 task**：手動建立 `tasks.register("checkstyleDebug", Checkstyle)` 並指定 `source = android.sourceSets.main.java.srcDirs`
  3. 改用 **detekt + Android Lint** 為主（兩者對 Android module 開箱即用）
- `subprojects { apply plugin: 'checkstyle' }` 套到 `buildSrc` 或純資源 module 會 fail，建議加 `if (project.plugins.hasPlugin('java-library') || project.name != 'buildSrc')` 守門。

### 12.4 Jacoco coverage
- 原本 `coverage: '/Total.*?([0-9]{1,3})%/'` regex **永遠抓不到**，因為 `jacocoTestReport` 預設不印百分比到 stdout。
- 本版已加上 inline Python 解析 `jacocoTestReport.xml` 並輸出 `Total coverage: NN%`，若 image 無 `python3` 請改寫成 shell + `xmllint`，或在 Gradle 加自訂 task：
  ```groovy
  jacocoTestReport.doLast {
      def report = file("build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
      // parse 出 INSTRUCTION counter 並 println "Total coverage: ${pct}%"
  }
  ```

### 12.5 GitLab 版本 / Tier
- `Security/SAST.gitlab-ci.yml`、`Security/Dependency-Scanning.gitlab-ci.yml` 部分掃描器需要 **Premium / Ultimate** tier；Free tier 會自動降級或 skip 部分 analyzer，**這不是設定錯誤**。
- `Secret Detection` 在 Free tier 完整可用。
- 若團隊為 Free tier，可移除 `Dependency-Scanning` template，改用 OWASP Dependency-Check Gradle plugin 在 lint stage 跑。

### 12.6 第三方套件版本
- §十一 `findsecbugs-plugin:1.13.0` 版本可能過舊，請至 [Maven Central](https://central.sonatype.com/artifact/com.h3xstream.findsecbugs/findsecbugs-plugin) 確認最新。
- Checkstyle / PMD / Detekt 版本同理，定期升級。

### 12.7 Pre-commit hook 效能
- §三 `pre-push` 跑 `lintDebug + testDebugUnitTest`，**大型專案可能 5–10 分鐘**，開發者會偷偷停用 hook。建議：
  - 拆分 commit hook（快、style + static）與 push hook（慢、test）
  - 提供 `--no-verify` 例外規範（緊急 bypass 流程）
  - 或改在 IDE 用「Save Actions」+ Gradle daemon 預熱降低延遲

### 12.8 共享 git hook 的權限
- `installGitHooks` task 用 Groovy 的 `fileMode = 0755`，Kotlin DSL 或 Gradle 8.3+ 應改 `filePermissions { unix("0755") }`，否則 Linux/macOS 上 hook 會 **沒有執行權限** 而靜默失效。
- Windows 開發者：git hook 走 git bash，`#!/bin/sh` shebang 可運作；但 hook 檔不需 chmod。

---

## 十三、檢查清單

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
