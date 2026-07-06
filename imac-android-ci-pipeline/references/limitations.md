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

## 工具版本與相容性

下表是「Gradle 8.10 + JDK 17（CI image）+ Java 8 target」環境下、2026-06 驗證過的 baseline。**每次使用本 skill 時應依 `references/version-check.md` 用 web 重新查證當前相容版本**；本表作為查不到 web 時的 fallback 與相容規則參考：

| 元件 | 鎖定版本 | 為什麼不是 latest |
|---|---|---|
| `com.github.spotbugs`（Gradle plugin）| 6.3.0 | 6.4.0+ 升 Kotlin 2.2.20 metadata，Gradle 8.x 內建 Kotlin 讀不了，會在 apply plugin 階段就失敗。6.3.0 是最後支援 Gradle 8 的版本。升級 Gradle 到 9 才能用 6.4.0+。 |
| SpotBugs（`toolVersion`）| 4.8.6 | find-sec-bugs 1.14.0 針對 SpotBugs 4.8.x 建置，pin 4.8.x 對齊引擎，避免 4.9/4.10 的 API 落差。 |
| `findsecbugs-plugin` | 1.14.0 | 目前最新，提供 SAST 安全規則。 |
| Checkstyle（`toolVersion`）| 10.21.1 | 10.x 需 Java 11+，可在 image 的 JDK 17 上跑；13.x 需 Java 21，會跑不動。鎖在 10.x 系列。 |
| JaCoCo | 0.8.12 | 支援到 Java 22，JDK 17 可跑。 |
| `mingc/android-build-box` | 1.27.0 | 內含 JDK 17，相容 AGP 8.2。正式環境建議鎖 digest，避免上游覆蓋 tag。 |

> 升級準則：動 Gradle / AGP / JDK 任一個版本前，先回頭比對這張表，尤其是 SpotBugs plugin 與 Gradle 的綁定關係，以及 Checkstyle 與 JDK 的最低版需求。

## 不涵蓋

- CD：部署、release 簽名、發佈到任何通道。
- SCA（第三方依賴漏洞掃描）、Instrumentation/Espresso（需 KVM 或 Firebase Test Lab）。
