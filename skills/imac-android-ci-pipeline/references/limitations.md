# 適用前提與限制

本範本以下列假設設計。複製到其他專案前先比對：

## 專案結構

- **單一 `app` module**。artifact 路徑寫死 `app/build/...`。多 module 專案需改用 glob，且 `coverage_report` 不支援 glob，要逐 module 處理或合併報告。
- module 名為 `app`。若是別的名字（`mobile`、`presentation`），`.gitlab-ci.yml` 與 `gradle-setup.md` 內的路徑要一起換。

## Gradle DSL

- 範例為 Groovy DSL（`build.gradle`）。若專案用 Kotlin DSL（`build.gradle.kts`）需轉語法：
  - `tasks.register('checkstyle', Checkstyle) { ... }` → `tasks.register<Checkstyle>("checkstyle") { ... }`
  - `rootProject.file('...')` → `rootProject.file("...")`

## 語言

- 範本針對 **純 Java** 專案，只含 Checkstyle + SpotBugs + Android Lint。
- 若加入 Kotlin 原始碼，另需 ktlint（風格）與 detekt（靜態分析）；兩者對 Android module 開箱即用，可在 lint / sast stage 各加一個 job，並用 `rules: exists "**/*.kt"` 控制只在有 Kotlin 時跑。

## AGP / 路徑

- `spotbugs` 與 `jacocoTestReport` 的 class 來源取自 `compileDebugJavaWithJavac` 的 `destinationDirectory`，不靠寫死 `intermediates` 路徑，AGP 升級時通常不需改。
- `jacocoTestReport` 的 `executionData` 指向 `build/jacoco`（unit test 的 `.exec` 位置），不要掃整個 `build/`，否則 Gradle 嚴格驗證會因隱性依賴報錯。
- AGP 大版本升級後仍建議照 `gradle-setup.md` 的「首次驗證」實跑一次。

## 相容規則

版本號不寫在這裡——數字會腐化，規則不會。下表是決定版本時要套用的相依關係；查證流程與推導步驟見 `references/version-check.md`。

| 元件 | 相依對象 | 規則 |
|---|---|---|
| `com.github.spotbugs`（Gradle plugin）| Gradle 主版本 | 已知分界點：6.4.0 起改用新的 Kotlin metadata，Gradle 8.x 內建 Kotlin 讀不了；Gradle 8.x 取 6.4.0 之前的最新版，Gradle 9.x 可用 6.4.0 以上 |
| SpotBugs（`toolVersion`）| `findsecbugs-plugin` 針對的 SpotBugs 版本 | 與 find-sec-bugs release notes 記載的「Upgrade SpotBugs to X」對齊主.次版 |
| `findsecbugs-plugin` | — | 取最新穩定版，無額外相依限制 |
| Checkstyle（`toolVersion`）| 建置 JDK | 主版本有最低 JDK 需求。已知：10.x 需 Java 11+、13.x 需 Java 21。取建置 JDK 跑得動的最新版 |
| JaCoCo | 建置 JDK | 取支援建置 JDK class 版本的最新版 |
| CI image（`ghcr.io/cirruslabs/android-sdk`）| 專案 `compileSdk` | tag = compileSdk 數字；需要 NDK 用 `<compileSdk>-ndk` |

> 升級準則：動 Gradle / AGP / JDK / compileSdk 任一個之前，先回頭比對這張表，尤其是 SpotBugs plugin 與 Gradle 的綁定關係，以及 Checkstyle 與 JDK 的最低版需求。

## 不涵蓋

- CD：部署、release 簽名、發佈到任何通道。
- SCA（第三方依賴漏洞掃描）、Instrumentation/Espresso（需 KVM 或 Firebase Test Lab）。
