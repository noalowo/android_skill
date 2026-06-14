# 版本查證（套用前必做）

使用本 skill 時，把任何版本號寫進 `build.gradle` 或 `.gitlab-ci.yml` 之前，**先用 web 查證「當前最新且與本專案相容」的版本**，不要直接照抄本 skill 的 baseline 數字——數字會過期，相容性也會隨 Gradle / JDK 改變。

`references/limitations.md` 的版本表是 Gradle 8.10 / JDK 17 / AGP 8.2 環境下、2026-06 驗證過的 baseline，作為查不到 web 時的 fallback。

## 1. 先確認專案的硬限制

這些決定其他工具的版本上限：

| 限制 | 從哪讀 |
|---|---|
| Gradle 版本 | `gradle/wrapper/gradle-wrapper.properties` 的 `distributionUrl` |
| AGP 版本 | root `build.gradle` 的 `com.android.application` version |
| 建置 JDK | CI image 的 JDK（`mingc/android-build-box:1.27.0` = JDK 17），以及本機 / Android Studio 的 JDK。工具跑在建置 JDK 上，不是專案的 Java target |
| Java target | `app/build.gradle` 的 `compileOptions`（只影響 bytecode，不影響工具能否執行）|

## 2. 逐工具查證 + 相容規則

用 web search 或 GitHub releases 查當前版本，並套用下面的相容規則（這些關係才是重點，不是只看「最新」）：

| 工具 | 查哪裡 | 相容規則 |
|---|---|---|
| `spotbugs-gradle-plugin` | GitHub `spotbugs/spotbugs-gradle-plugin` releases | 必須相容專案的 Gradle 主版本。Gradle 8.x → 用 6.4.0 之前的版本（6.4.0+ 升 Kotlin 2.2.20 metadata，Gradle 8 內建 Kotlin 讀不了）；Gradle 9.x → 可用 6.4.0+。查 release notes 是否提到 drop Gradle 8 / Kotlin bump |
| `findsecbugs-plugin` | Maven Central / find-sec-bugs releases | 取最新穩定版，並記下該版「針對的 SpotBugs 版本」（看 release notes 的 "Upgrade SpotBugs to X"），回頭決定下一列 |
| SpotBugs（`toolVersion`）| SpotBugs releases | 與 find-sec-bugs 針對的 SpotBugs 主.次版對齊（例：find-sec-bugs 1.14.0 → SpotBugs 4.8.x）|
| Checkstyle（`toolVersion`）| checkstyle releases | 必須能在建置 JDK 上執行：10.x 需 Java 11+、13.x 需 Java 21。JDK 17 → 鎖 10.x 最新版 |
| JaCoCo | jacoco change history | 要支援建置 JDK 的 class 版本 |
| `mingc/android-build-box` | Docker Hub tags | 取含所需 JDK / compileSdk 的穩定 tag；正式環境鎖 digest |

## 3. 決策順序

1. 先定 Gradle / AGP / JDK（專案既有，通常不動）。
2. 依 Gradle 主版本定 `spotbugs-gradle-plugin` 上限。
3. 取 find-sec-bugs 最新版，回推 SpotBugs `toolVersion`。
4. 依建置 JDK 定 Checkstyle / JaCoCo 上限。
5. 查不到 web 或離線時，用 `limitations.md` 的 baseline。

## 4. 版本對了之後仍要本機驗證

版本相容不等於一定跑得起來。AGP 的 `intermediates` 輸出路徑等仍需照 `gradle-setup.md` 的「首次驗證」實跑一次確認。
