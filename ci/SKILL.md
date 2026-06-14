---
name: android-ci-pipeline
description: Use when setting up, modifying, or troubleshooting a CI pipeline for a Java Android project (single app module) on GitLab (gitlab.com or self-hosted, Free tier). Triggers on Chinese or English "設定 GitLab CI / 建立 pipeline / 加入 lint / 單元測試階段 / 程式碼品質檢查 / Checkstyle / SpotBugs / find-sec-bugs / SAST / 靜態安全檢測 / build APK / debug APK / pre-commit hook / MR pipeline", "setup CI", "add GitLab CI", "configure pipeline", "code quality check", "add SAST", "build debug APK", "pre-commit hook". 涵蓋 lint / 程式碼品質 / SAST / 單元測試 / build debug APK。不含 CD（部署 / 發佈 / release 簽名）。
version: 4.0.0
---

# Android CI Pipeline（GitLab，Java、單一 app module）

為 Java Android 專案在 GitLab（Free tier）上建立 CI pipeline：自動跑程式碼品質檢查、SAST、單元測試，最後 build 出 debug APK。本 skill 不含 CD（部署、發佈、release 簽名）。

預設目標環境：單一 `app` module、Groovy DSL、`com.android.application`、GitLab Free tier。其他結構請先讀 `references/limitations.md` 比對假設。

## 觸發後第一件事

依使用者意圖定位到對應檔案，不需要全讀：

| 意圖 | 動作 |
|---|---|
| 第一次建 CI、要可直接用的 pipeline | 照「快速開始」走，複製 `assets/` 檔案 |
| 要決定工具版本（每次使用都先做）| 讀 `references/version-check.md`，用 web 查證當前相容版本 |
| 在 Android module 上接 Checkstyle / SpotBugs / JaCoCo | 讀 `references/gradle-setup.md` |
| 設定 GitLab 分支保護、MR 守門、Free tier 的 SAST 行為 | 讀 `references/gitlab-setup.md` |
| 讓 Android Studio 的檢查與 CI 一致 | 讀 `references/android-studio.md` |
| pipeline 掛了、task not found、cache miss、覆蓋率不顯示 | 讀 `references/troubleshooting.md` |
| 想確認本範本是否適用自己的專案 | 讀 `references/limitations.md` |

## Pipeline 一覽

```
lint  →  sast  →  test  →  build
```

| Stage | 工具 | Gradle 指令 | 對應目標 |
|---|---|---|---|
| lint | Checkstyle + Android Lint | `./gradlew checkstyle lintDebug` | 程式碼品質 / 風格 |
| sast | SpotBugs + find-sec-bugs | `./gradlew spotbugs` | 靜態安全檢測 |
| test | JUnit + JaCoCo | `./gradlew testDebugUnitTest jacocoTestReport` | 單元測試 + 覆蓋率 |
| build | Gradle | `./gradlew assembleDebug` | debug APK artifact |

## 概念對照（商用工具 → 本 skill 的免費工具）

SAST 是「靜態安全檢測」這個能力的概念，不是特定產品。Fortify、WhiteSource(Mend) 是商用實作；本 skill 用免費工具達到同一能力。

| 安全 / 品質概念 | 商用工具 | 本 skill |
|---|---|---|
| SAST（掃自己的原始碼找漏洞）| Fortify | SpotBugs + find-sec-bugs |
| 程式碼品質 / 風格 | SonarQube 等 | Checkstyle + Android Lint |
| 單元測試覆蓋率 | — | JaCoCo |

WhiteSource(Mend) 屬於 SCA（掃第三方依賴），不在本 skill 範圍；需要時用 GitLab Dependency Scanning 或 OWASP Dependency-Check。

## 快速開始

1. 複製 `assets/gitlab-ci.yml` 到專案根目錄並命名為 `.gitlab-ci.yml`。
2. 在專案根建立 `config/` 並放入規則檔：
   - `config/checkstyle/checkstyle.xml`（複製 `assets/checkstyle.xml`）
   - `config/spotbugs/exclude.xml`（複製 `assets/spotbugs-exclude.xml`）
3. 複製 `assets/editorconfig` 到專案根並命名為 `.editorconfig`。
4. 依 `references/version-check.md`，用 web 查證本專案環境下各工具的當前相容版本，再決定要寫進 `build.gradle` 的版本號（不要直接照抄 baseline）。
5. 依 `references/gradle-setup.md`，把 Checkstyle / SpotBugs + find-sec-bugs / JaCoCo 的設定（套用上一步查到的版本）加進 `app/build.gradle`。
6. 第一次先在本機驗證每個 task 都跑得起來（這步最關鍵，Android module 的 task 路徑需確認）：
   ```
   ./gradlew checkstyle spotbugs lintDebug testDebugUnitTest jacocoTestReport assembleDebug
   ```
7. 全綠後 push，到 GitLab 看 pipeline。
8. 依 `references/gitlab-setup.md` 設定分支保護與 MR 守門。
9. （選用）複製 `scripts/git-hooks/` 內的 hook 到 `.git/hooks/` 當本地守門員。

> 導入既有專案時程式碼通常已累積違規。預設用 Gradle 端的軟性門檻先報告不擋路（Checkstyle `severity=warning`、SpotBugs `ignoreFailures=true`），團隊清乾淨後再收緊成硬性門檻。詳見 `references/troubleshooting.md` 的 ratchet 流程。

## 檢查清單

- [ ] 已依 `references/version-check.md` 用 web 查證並決定各工具的相容版本
- [ ] `.gitlab-ci.yml` 已放在專案根
- [ ] `config/checkstyle/checkstyle.xml`、`config/spotbugs/exclude.xml` 已建立
- [ ] `app/build.gradle` 已加 checkstyle / spotbugs / jacoco 設定與自訂 task
- [ ] 本機 `./gradlew checkstyle spotbugs lintDebug testDebugUnitTest jacocoTestReport assembleDebug` 全綠
- [ ] `GRADLE_USER_HOME` 指向 `$CI_PROJECT_DIR/.gradle`
- [ ] find-sec-bugs plugin 已加入 `spotbugsPlugins`
- [ ] `main` / `develop` 設為 protected branch、MR 啟用「Pipelines must succeed」
- [ ] build stage 只產 debug APK（無 release 簽名、無 keystore 變數）
