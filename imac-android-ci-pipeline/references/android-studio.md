# Android Studio 整合

讓 IDE 內的檢查與 CI 規則一致，避免「本地過、CI 掛」。

## IDE 外掛

| 外掛 | 用途 |
|---|---|
| CheckStyle-IDEA | 即時 Checkstyle 檢查 |
| SpotBugs IDEA | 即時 SpotBugs 檢查 |

安裝後指向專案內同一份規則檔，IDE 與 CI 才一致：

- Settings → Tools → Checkstyle → 加入 `config/checkstyle/checkstyle.xml`
- SpotBugs 外掛同理指向 `config/spotbugs/exclude.xml`

## Code Style

- Settings → Editor → Code Style → 啟用 `.editorconfig`（預設已開），IDE 會自動套用專案根的 `.editorconfig`
- 開啟「Reformat on save」前，先確認 IDE 格式化結果與 `checkstyle.xml` 一致，否則會出現 IDE 改完反而 Checkstyle 報錯

## Gradle 面板

從 Android Studio 右側 Gradle 面板可直接執行與 CI 相同的 task：

- `verification → testDebugUnitTest`
- `other → checkstyle` / `spotbugs`
- `verification → lintDebug`

## Git Hook 註記

git hook 由 git 本身執行 `.git/hooks/`，不靠 IDE 外掛。GitToolBox 之類只負責狀態列顯示，不會執行 hook。
