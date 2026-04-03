---
name: android-ci-pipeline
description: Use when setting up, modifying, or troubleshooting a CI pipeline for an Android project. Triggers on "setup CI", "add GitHub Actions", "create pipeline", "configure CI/CD", "automate build", "add lint check", "add unit test stage", "add instrumentation test", "build APK in CI", or any request involving continuous integration workflow for Android.
version: 1.0.0
---

# Android CI Pipeline（GitHub Actions）

此 skill 適用於為 Android Java/Kotlin 專案設定 GitHub Actions CI pipeline。

## Pipeline 架構

```
push / PR → lint → unit-test → instrumentation-test → static-analysis → build-apk
```

每個 job 使用 `needs:` 串聯，前一個 job 失敗則後續全部停止。

## 完整 YAML 範本

檔案路徑：`.github/workflows/ci.yml`

```yaml
name: CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:

  # ── Stage 1：Lint ──────────────────────────────────────
  lint:
    name: Lint Check
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Cache Gradle
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
          restore-keys: ${{ runner.os }}-gradle-

      - name: Make gradlew executable
        run: chmod +x ./gradlew

      - name: Run lint
        run: ./gradlew lintDebug

      - name: Upload lint report
        uses: actions/upload-artifact@v3
        with:
          name: lint-report
          path: app/build/reports/lint-results-debug.html

  # ── Stage 2：Unit Test ─────────────────────────────────
  unit-test:
    name: Unit Test
    needs: [ lint ]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Cache Gradle
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
          restore-keys: ${{ runner.os }}-gradle-

      - name: Make gradlew executable
        run: chmod +x ./gradlew

      - name: Run unit tests
        run: ./gradlew testDebugUnitTest

      - name: Upload test report
        uses: actions/upload-artifact@v3
        with:
          name: unit-test-report
          path: app/build/reports/tests/testDebugUnitTest/

  # ── Stage 3：Instrumentation Test ─────────────────────
  instrumentation-test:
    name: Instrumentation Test
    needs: [ unit-test ]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Make gradlew executable
        run: chmod +x ./gradlew

      - name: Run instrumented tests (Espresso)
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 29
          arch: x86_64
          script: ./gradlew connectedCheck

      - name: Upload instrumentation report
        uses: actions/upload-artifact@v3
        with:
          name: instrumentation-test-report
          path: app/build/reports/androidTests/connected/

  # ── Stage 4：Static Code Analysis ─────────────────────
  static-analysis:
    name: Static Analysis (SonarCloud)
    needs: [ instrumentation-test ]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0           # SonarCloud 需要完整 git history

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Make gradlew executable
        run: chmod +x ./gradlew

      - name: SonarCloud Scan
        run: ./gradlew app:sonarqube -Dsonar.login=${{ secrets.SONAR_TOKEN }}
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}

  # ── Stage 5：Build Debug APK ───────────────────────────
  build-apk:
    name: Build Debug APK
    needs: [ static-analysis ]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Make gradlew executable
        run: chmod +x ./gradlew

      - name: Build debug APK
        run: ./gradlew assembleDebug --stacktrace

      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: debug-apk
          path: app/build/outputs/apk/debug/app-debug.apk
```

## 各 Stage 說明

| Stage | 工具 | Gradle 指令 | Runner |
|---|---|---|---|
| lint | Android Lint | `./gradlew lintDebug` | ubuntu-latest |
| unit-test | JUnit | `./gradlew testDebugUnitTest` | ubuntu-latest |
| instrumentation-test | Espresso + Emulator | `./gradlew connectedCheck` | ubuntu-latest |
| static-analysis | SonarCloud | `./gradlew app:sonarqube` | ubuntu-latest |
| build-apk | Gradle | `./gradlew assembleDebug` | ubuntu-latest |

> **注意：** instrumentation-test 需指定 `arch: x86_64`，ubuntu runner 才能正確啟動 KVM 加速的模擬器。

## Gradle Cache 設定

所有 job 都應加上 cache，避免每次重複下載依賴：

```yaml
- name: Cache Gradle
  uses: actions/cache@v3
  with:
    path: |
      ~/.gradle/caches
      ~/.gradle/wrapper
    key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
    restore-keys: ${{ runner.os }}-gradle-
```

## Secrets 設定

在 GitHub repo → Settings → Secrets and variables → Actions 新增：

| Secret 名稱 | 用途 |
|---|---|
| `SONAR_TOKEN` | SonarCloud 掃描認證 token |
| `KEYSTORE_FILE` | release 簽名用（CD 階段） |
| `KEY_ALIAS` | release 簽名用 |
| `KEY_PASSWORD` | release 簽名用 |
| `STORE_PASSWORD` | release 簽名用 |

## 常見問題

| 問題 | 原因 | 解法 |
|---|---|---|
| `Permission denied: ./gradlew` | gradlew 未設執行權限 | 加 `chmod +x ./gradlew` 步驟 |
| Emulator 啟動失敗 | ubuntu 不支援 KVM 加速 | instrumentation-test 改用 `macos-latest` |
| SonarCloud 找不到覆蓋率 | fetch-depth 不完整 | checkout 加 `fetch-depth: 0` |
| Gradle build cache miss | key 沒包含 wrapper properties | key 加 `gradle-wrapper.properties` hash |

## 檢查清單

設定或修改 CI pipeline 後，確認：
- [ ] `.github/workflows/ci.yml` 已建立
- [ ] 每個 job 都有 `needs:` 正確串聯
- [ ] instrumentation-test job 使用 `macos-latest`
- [ ] 所有 job 都有 Gradle cache 設定
- [ ] `gradlew` 每個 job 都有 `chmod +x`
- [ ] artifact 路徑與實際 build 輸出路徑一致
- [ ] SonarCloud 的 `SONAR_TOKEN` 已在 GitHub Secrets 設定
- [ ] `fetch-depth: 0` 用於 SonarCloud job
