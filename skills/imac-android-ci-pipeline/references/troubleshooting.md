# 常見問題

| 問題 | 原因 | 解法 |
|---|---|---|
| `Task 'checkstyleMain' not found` | Android module 沒有 Java plugin 的 source set 任務 | 用 `references/gradle-setup.md` 的自訂 `checkstyle` task，指令改 `./gradlew checkstyle` |
| `spotbugs` 找不到 class / 報告為空 | AGP 的 javac 輸出路徑與範例 glob 不符 | 先 `./gradlew compileDebugJavaWithJavac`，到 `app/build/intermediates/javac/debug/` 看實際子目錄，調整 `classDirs` 的 glob |
| `jacocoTestReport` 覆蓋率為 0 | 找不到 `.exec` 執行資料，或 class 路徑不對 | 確認 `testDebugUnitTest` 有先跑；檢查 `executionData` 與 `classDirectories` 路徑；`debug { testCoverageEnabled true }` 已設 |
| 覆蓋率不顯示在 MR | `coverage` regex 抓不到 | 確認 jacoco task 的 `doLast` 有印 `Total coverage: NN%`，且 `.gitlab-ci.yml` 的 `coverage:` regex 一致 |
| Cache 每次 miss | `GRADLE_USER_HOME` 沒設在專案內 | 設 `GRADLE_USER_HOME=$CI_PROJECT_DIR/.gradle` |
| `Permission denied: ./gradlew` | gradlew 無執行權限 | `before_script` 加 `chmod +x ./gradlew` |
| pipeline 跑兩次 | branch 與 MR 同時觸發 | `workflow:rules` 對 branch rule 加 `&& $CI_PIPELINE_SOURCE != "merge_request_event"` |
| Checkstyle / SpotBugs 報太多既有違規擋住 pipeline | 既有專案累積的違規 | 用 Gradle 軟性門檻：Checkstyle `severity=warning`、SpotBugs `ignoreFailures=true`，先報告不擋路 |
| SpotBugs false positive | 規則太嚴 | 在 `config/spotbugs/exclude.xml` 加 `<Match>` 排除 |
| apply spotbugs plugin 報 Kotlin metadata 版本錯誤 | spotbugs-gradle-plugin 版本與 Gradle 主版本不相容（已知分界點：6.4.0 起的 Kotlin metadata，Gradle 8.x 讀不了）| 依 `references/version-check.md` 的相容規則，依專案的 Gradle 主版本重新推導 plugin 版本上限 |
| Checkstyle 在 CI 報 `UnsupportedClassVersionError` | Checkstyle 版本需要的 JDK 高於建置 JDK | 依 `references/version-check.md`，用 image 實查得到的建置 JDK 反推 Checkstyle 主版本上限，不要假設固定鎖在某個主版本 |
| `build-debug` 報 `No space left on device`（packageDebug）| runner 磁碟不足：大型 image + gradle cache + 大型原生庫（tensorflow / mediapipe 等）+ APK 塞爆 small runner（25GB）| 見下方「build 磁碟不足」|

## 把品質門檻收緊（ratchet）

導入既有專案時用 Gradle 端軟性門檻：Checkstyle `severity=warning`、SpotBugs `ignoreFailures=true`。pipeline 會跑、會報告，但不擋合併。建議流程：

1. 先讓團隊看報告，分批修掉違規。
2. SpotBugs 清乾淨後把 `spotbugs { ignoreFailures = false }`，變成硬性門檻。
3. Checkstyle 清乾淨後把 `checkstyle.xml` 的 `severity` 從 `warning` 改 `error`，讓新違規直接讓 task 失敗。

## build 磁碟不足（No space left on device）

帶大量原生庫的 app（tensorflow-lite / mediapipe / vosk 等）打包時，APK 與 intermediates 很大，加上 image 本身與 gradle cache 的體積，容易塞爆磁碟小的 runner。image 大小差異很大：精簡的 SDK-only image 約 1GB 級，含完整工具鏈的全家桶型 image 壓縮後約 6GB 級——選精簡 image 本身就是緩解磁碟不足的方法之一。lint / sast / test 等輕量 job 不受影響，只有 `build-debug`（packageDebug）會爆。處理方式依 runner 類型：

- **gitlab.com SaaS**：small runner 只有 25GB。把 `build-debug` 指到磁碟較大的 runner：
  ```yaml
  build-debug:
    tags:
      - saas-linux-medium-amd64   # 50GB，Free tier 可用；large(100GB) 需付費
  ```
  只套在 build job，其他 job 留 small 省 CI 分鐘。medium 的 CI 分鐘成本倍率高於 small。
- **自架 runner**：確保 VM 磁碟足夠（建議 30–50GB 以上空餘），或定期清 docker / gradle cache。
