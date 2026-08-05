# 版本查證（套用前必做）

規則留下，數字刪掉：本文教的是「怎麼推導」本專案當下相容的版本與 image，不列出建議版本號。任何版本號寫進 `build.gradle` 或 `.gitlab-ci.yml` 之前，一律走完下面的推導鏈（Step 0 → 4），不得抄本 skill 文件內出現過的任何歷史版本數字。

## Step 0：讀專案硬限制

這些決定其他工具版本的上限，一律從專案本身讀，不從本文件讀：

| 限制 | 來源 |
|---|---|
| Gradle 版本 | `gradle/wrapper/gradle-wrapper.properties` 的 `distributionUrl` |
| AGP 版本 | `gradle/libs.versions.toml` 的 `[versions] agp`，或 root `build.gradle` 的 `com.android.application` version |
| compileSdk | `app/build.gradle` 的 `compileSdk` |
| Java target | `app/build.gradle` 的 `compileOptions` |

## Step 1：決定 CI image

`ghcr.io/cirruslabs/android-sdk` 的 tag 就是 Android API level，可由專案的 `compileSdk` 直接推導：

- image = `ghcr.io/cirruslabs/android-sdk:<compileSdk>`
- 需要 NDK 時用 `<compileSdk>-ndk` 變體

解析 digest（正式環境鎖 digest，避免上游覆蓋 tag）：

```bash
token=$(curl -s "https://ghcr.io/token?scope=repository%3Acirruslabs%2Fandroid-sdk%3Apull&service=ghcr.io" | sed -E 's/.*"token":"([^"]+)".*/\1/')
curl -sI -H "Authorization: Bearer $token" \
  -H "Accept: application/vnd.oci.image.index.v1+json" \
  "https://ghcr.io/v2/cirruslabs/android-sdk/manifests/<TAG>" | grep -i docker-content-digest
```

digest 是內容雜湊，跨 registry 不變；鏡像到內部 registry 後仍可用同一 digest 驗證一致性。

**建置 JDK 必須從 image 實查得來，不是假設**——它決定了後面 Checkstyle / JaCoCo 的版本上限。兩種查法：

- 有 Docker 環境：`docker run --rm <image> java -version`
- 離線 / 無 Docker：讀 image 的 config blob，找安裝紀錄或 `Env` 裡的 JDK 套件版本（例如套件名裡的 `openjdk-<N>-jdk`）

## Step 2：依相容規則推導工具版本

以下是相依關係與規則，不是建議版本——用規則配合當下查到的 release 資訊，推出當下環境該用的版本號：

| 工具 | 查哪裡 | 相容規則 |
|---|---|---|
| `spotbugs-gradle-plugin` | GitHub `spotbugs/spotbugs-gradle-plugin` releases | 必須支援專案的 Gradle 主版本。已知分界點：6.4.0 起改用新的 Kotlin metadata，Gradle 8.x 內建 Kotlin 讀不了 → Gradle 8.x 取 6.4.0 之前的最新版；Gradle 9.x 可用 6.4.0 以上。查 release notes 是否出現更新的分界點 |
| `findsecbugs-plugin` | Maven Central / find-sec-bugs releases | 取最新穩定版，並從 release notes 記下它「針對的 SpotBugs 版本」（通常寫成 "Upgrade SpotBugs to X"）|
| SpotBugs（`toolVersion`）| SpotBugs releases | 與上一步 find-sec-bugs 針對的 SpotBugs 主.次版對齊 |
| Checkstyle（`toolVersion`）| checkstyle releases | 主版本有最低 JDK 需求。已知：10.x 需 Java 11+、13.x 需 Java 21。取「建置 JDK 跑得動」的最新版——JDK 版本更高時通常能取更高的 Checkstyle 主版本，不要預設鎖在某個主版本 |
| JaCoCo | jacoco change history | 取支援建置 JDK class 版本的最新版 |

決策順序：Step 0 定 Gradle / AGP / compileSdk（專案既有，通常不動）→ Step 1 定 image 與建置 JDK → 依 Gradle 主版本定 `spotbugs-gradle-plugin` 上限 → 取 find-sec-bugs 最新版回推 SpotBugs `toolVersion` → 依建置 JDK 定 Checkstyle / JaCoCo。

## Step 3：離線 / 查不到 web 時的 fallback

不得採用本 skill 文件內出現過的歷史版本數字當 fallback——那些數字只是特定時間點的快照，套到現在的環境很可能已經不相容，這正是版本會腐化的原因。

- **既有專案**：以專案目前 `build.gradle` 裡已經在用、且確認能跑的版本為起點，套用 Step 2 的相容規則往回推是否需要調整（例如 Gradle 升級後 `spotbugs-gradle-plugin` 的上限是否跟著變動）。
- **全新專案**：用 Step 1 查到的 image 內建 JDK 能跑得動的保守組合起步，不要憑印象猜版本號，靠 Step 4 實跑驗證是否成立，跑不過再依 Step 2 規則調整。

## Step 4：實跑驗證（硬性 gate，不是建議）

版本相容不等於一定跑得起來，Step 2 推出的版本組合必須實跑通過才算數：

```bash
./gradlew checkstyle spotbugs lintDebug testDebugUnitTest jacocoTestReport assembleDebug
```

全綠才能視為版本推導完成。路徑相關細節（`intermediates`、`compileDebugJavaWithJavac` 等）見 `references/gradle-setup.md`；跑不過時的錯誤對照見 `references/troubleshooting.md`。
