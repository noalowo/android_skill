---
description: 建立一個新的 MVP 模組骨架（Contract / Presenter / Activity / layout 範本檔）
argument-hint: "<package_path> <module_name> <ModuleName> <full_package>"
---

執行 imac-android plugin 的 `create_mvp_module.sh`，快速產生 MVP 模組骨架檔案。

腳本位置（plugin 內）：`${CLAUDE_PLUGIN_ROOT}/skills/imac-android-architecture-skill/scripts/create_mvp_module.sh`

執行：

```
bash "${CLAUDE_PLUGIN_ROOT}/skills/imac-android-architecture-skill/scripts/create_mvp_module.sh" $ARGUMENTS
```

參數：`<package_path> <module_name> <ModuleName> <full_package>`
（例：`app/src/main/java/com/example/app ui/settings Settings com.example.app`）

缺參數時先依專案 package 結構推斷或詢問。產生後，依 `imac-android-architecture-skill` 的規範補齊各檔案的 TODO 內容。
