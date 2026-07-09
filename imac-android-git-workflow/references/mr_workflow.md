# 建立 Merge Request 完整流程 / Full MR Workflow

從開分支到 MR 合併、再到本地清理的標準流程。

---

## Step 1. 從最新 develop 開 feature 分支

### 1.1 對齊本地 develop 到遠端

```bash
git checkout develop
git fetch origin
git reset --hard origin/develop
```

> ⚠️ **不要用 `git pull`**。本地 develop 一律靠 `fetch + reset --hard` 對齊遠端，避免本地多餘 commit 干擾後續流程。

### 1.2 建立 feature 分支

```bash
git checkout -b feature/<dev>_<area>
```

命名格式參見 `SKILL.md` §3.1。

### 1.3 開發與提交

按功能單元做 commit，每個 commit 都要符合 `SKILL.md` §3.2 的格式：

```
[<type>] <簡述> - <developer>
```

---

## Step 2. 推送並建立 MR

### 2.1 首次推送

```bash
git push -u origin feature/<dev>_<area>
```

`-u` 設定 upstream，之後可直接 `git push`。

### 2.2 在 GitLab 建立 MR

到 GitLab 專案頁面 → 從剛推送的分支建立 Merge Request：

- **Source**: `feature/<dev>_<area>`
- **Target**: `develop`
- **Merge method**: Merge commit with semi-linear history（專案層級已設定）

### 2.3 MR 內容

| 欄位 | 內容 |
|---|---|
| 標題 | 同 commit message 標題格式：`[<type>] <簡述> - <developer>` |
| 說明欄 | 補充 Add / Fix / Refactor 條列 |
| 測試方式 | 視情況撰寫，描述如何驗證 |
| 風險點 | 視情況撰寫，已知問題、需注意處 |

### 2.4 勾選 "Delete source branch"

建立 MR 時勾起 **Delete source branch when merge request is accepted**。

---

## Step 3. MR 後續變更

MR 建立後，所有後續變更（review 修正、解衝突、加 commit）：

1. 在 feature 分支上新增 commit 或 rebase
2. 推到同一個遠端分支（rebase 後需要 `git push -f`）
3. GitLab 會自動更新 MR

如果 MR 顯示「需要更新分支」或 reviewer 要求對齊最新 develop → 參見 `rebase_guide.md`。

---

## Step 4. MR 合併後（由有權限者點 Merge）

### 4.1 GitLab 端

合併時確認 "Delete source branch" 已勾起。

### 4.2 本地清理

```bash
git checkout develop
git fetch origin
git reset --hard origin/develop          # 同步合併後的 develop
git branch -d feature/<dev>_<area>       # 刪本地 feature
git remote prune origin                  # 清遠端已刪分支的追蹤
```

> ⚠️ 不要用 `git pull --rebase origin develop`。即使原始文件這樣寫過，本團隊規則為一律 `fetch + reset --hard`。

---

## Step 5. 開始下一個功能

回到 Step 1，從乾淨的 develop 開新 feature 分支。
