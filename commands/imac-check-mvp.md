---
description: 檢查指定的 MVP 模組結構是否完整（Contract / Presenter / Activity / layout 等）
argument-hint: "<module_dir> <ModuleName>"
---

執行 imac-android plugin 的 `check_mvp_module.sh`，檢查一個 MVP 模組是否齊全。

腳本位置（plugin 內）：`${CLAUDE_PLUGIN_ROOT}/skills/imac-android-architecture-skill/scripts/check_mvp_module.sh`

執行：

```
bash "${CLAUDE_PLUGIN_ROOT}/skills/imac-android-architecture-skill/scripts/check_mvp_module.sh" $ARGUMENTS
```

參數：`<module_dir> <ModuleName>`
（例：`app/src/main/java/com/example/app/ui/settings Settings`）

若使用者沒給參數，先從目前專案結構推斷模組路徑與類別名，或直接詢問，再執行。把腳本輸出原樣回報。
