---
description: 驗證目前分支名稱與最後一筆 commit message 是否符合 Android Team Git 規範
argument-hint: "[branch <name> | commit \"<msg>\"]（留空則自動檢查目前分支＋最後一筆 commit）"
---

在使用者目前的 Android 專案根目錄，執行 imac-android plugin 內 git-workflow 的驗證腳本，檢查分支名與 commit message 格式。

腳本位置（plugin 內）：`${CLAUDE_PLUGIN_ROOT}/skills/imac-android-git-workflow/scripts/validate.sh`

執行：

```
bash "${CLAUDE_PLUGIN_ROOT}/skills/imac-android-git-workflow/scripts/validate.sh" $ARGUMENTS
```

未帶參數時腳本會自動檢查目前分支 + 最後一筆 commit。把腳本輸出原樣回報給使用者；若出現 ✗ 失敗項，依 `imac-android-git-workflow` skill 的規範簡述如何修正。
