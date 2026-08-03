---
name: imac-android-git-workflow
description: "Android team Git workflow: main/develop/feature branches, supports GitLab MR and GitHub PR. Use for branch creation, commits, MR/PR open or update, rebase, conflict resolution, merging to develop or main. MUST also trigger whenever the user is about to commit in an Android project, to enforce commit message format. Triggers on Chinese: 開新分支 / 分支命名 / commit / commit message 格式 / 開 MR / rebase develop / 解衝突 / 合併到 develop / 合併到 main / force push. Do not trigger for unrelated git questions."
version: 2.0.0
---

# Android 團隊 Git 協作規範

本 skill 規範 Android 團隊在 main / develop / feature 三分支模型下的協作流程，同時支援 GitLab 與 GitHub。Claude 讀完本檔後應能自行判斷分支操作、commit 格式、MR/PR 流程是否合規，並在使用者要求違規操作時直接拒絕。

---

## 0. 觸發後第一件事

被觸發時先判斷使用者意圖屬於哪一類，再決定要讀哪個 reference：

| 使用者意圖 | 動作 |
|---|---|
| 開新 feature 分支、推送、建立 MR/PR、MR/PR 後續變更、合併後清理、develop → main release | 讀 `references/workflow.md` |
| rebase、解衝突、force push、非標準狀況（commit 跑錯分支、想放棄改動、多人共用分支…） | 讀 `references/troubleshooting.md` |
| 要寫 commit message | 讀 `examples/commit_messages.md`，並用 `scripts/validate.sh` 驗證 |
| 要確認 GitLab / GitHub 專案設定是否正確 | 讀本檔 §5 |

簡單問題不需要全讀，定位到對應章節或檔案即可。

---

## 1. 紅線（絕不可違反）

如果使用者要求做下列任一項，直接拒絕並說明理由，不提供變通方案：

1. 禁止對 `main` 或 `develop` 直接 `git push` 或 `git push -f`（含 force push）
2. 禁止 feature 分支互相 merge（例如 `feature/a` merge 到 `feature/b`）

任一條被違反都可能造成歷史污染、責任歸屬不清、或丟失他人 commit。

---

## 2. 分支模型

```
main       release 版本（protected）
develop    整合區（protected）
feature/<developer>_<area>   個人開發分支
```

- `main`：release 版本，protected，只能透過 MR/PR 合併
- `develop`：整合區，protected，只能透過 MR/PR 合併
- `feature/*`：個人開發分支，無保護，合併後刪除

關鍵原則：

- 所有 feature 分支都從最新的 `develop` 開出
- feature 分支只 MR/PR 回 `develop`，不串接其他人的 feature（見紅線 2）
- MR/PR 合併後，來源 feature 分支刪除（平台端自動刪除 + 本地 `git branch -d`）
- `develop` → `main` 走 MR/PR，產生 merge commit，合併後在 `main` 打 tag（見 §5、`references/workflow.md`）

---

## 3. 命名規則

### 3.1 分支名

```
feature/<developer>_<area-or-feature-in-english>
```

- `<developer>`：開發者英文名，純小寫英數（可含 `.` `_` `-`）
- `<area-or-feature>`：自由命名的英文功能描述

合法：
```
feature/noah_login-api
feature/mary-jane_login-api
feature/alice_recyclerview-adapter
```

不合法：
```
feature/noah                # 缺功能描述
feature/登入                # 不可用中文
noah_login                  # 缺 feature/ 前綴
```

### 3.2 Commit message

格式：

```
[<type>] <當下主要變更的簡述> - <developer>

Add:
- ...
Fix:
- ...
```

- 第一行必填：type 必須小寫並用 `[]` 包住，結尾 `- <developer>` 不可省略
- type 固定 10 種：`feat` `fix` `refactor` `docs` `chore` `test` `style` `perf` `build` `ci`
- `Add:` / `Fix:` / `Refactor:` 條列 body 為選配，有其他變更才加
- `<developer>` 強制小寫

**取得 developer 名稱的方式**：Claude 不要每次都問使用者，先執行 `git config user.name`：

- 若結果是純小寫英數（可含 `.` `_` `-`）→ 直接採用
- 若不符合（含中文、含空格、有大寫字母等）→ 才詢問使用者一次

範例：
```
[feat] 新增登入 API 邏輯 - noah
```

不合法：
```
新增登入 API                       # 缺類型 tag、缺開發者
[feat] 新增登入 API                # 缺開發者
fix: 修正登入崩潰 - noah            # 類型應為 [fix] 而非 fix:
[feat] add login - Noah            # developer 必須小寫
```

驗證工具：`scripts/validate.sh` 可檢查分支名與 commit message。

---

## 4. 標準指令模板

### 開新 feature 分支

```bash
git checkout develop
git fetch origin
git reset --hard origin/develop
git checkout -b feature/<developer>_<area>
```

### 同步 protected 分支（develop / main）

一律用 `git fetch origin && git reset --hard origin/<branch>`，不要用 `git pull`：本地 `develop` 若累積雜 commit，`git pull` 會產生 merge commit，並被之後開出的 feature 分支繼承進 MR/PR；`reset --hard` 直接對齊遠端，避免這個問題。

執行 `reset --hard` 前先 `git status` 確認沒有未 commit 的改動——未 commit 的東西一旦被 reset 掉就救不回來。

```bash
git fetch origin
git status                       # 先確認沒有未 commit 的改動
git checkout <branch>
git reset --hard origin/<branch>
```

### 合併後本地清理

```bash
git checkout develop
git fetch origin
git reset --hard origin/develop
git branch -d feature/<developer>_<area>
git remote prune origin
```

---

## 5. 平台設定

以下設定只在專案尚未設定時才動手，先檢查現況，已設定就不要改動。

| 項目 | GitLab | GitHub |
|---|---|---|
| 分支保護（禁止直接 push） | Settings → Repository → Protected branches，把 `main`、`develop` 都設為 protected | Settings → Branches → Branch protection rules，對 `main`、`develop` 建立規則 |
| 合併方式 | Settings → Merge requests 設定 merge method；只允許 merge commit，關閉 squash | Settings → General → Pull Requests 區塊：勾選 Allow merge commits，取消勾選 Allow squash merging、Allow rebase merging |
| 合併後自動刪除來源分支 | 建立 MR 時勾選 "Delete source branch" | 勾選 "Automatically delete head branches" |
| 分支落後時的更新按鈕 | MR 頁面的 Rebase 按鈕 | PR 頁面的 Update branch 按鈕 |

---

## 6. Commit 時的行為

當使用者執行 `git commit`、要求產生 commit message、或貼 commit 草稿給你看時：

1. 檢查當前分支（`git branch --show-current`）：若在 `main` 或 `develop`，依紅線規則拒絕，並引導 `git checkout -b feature/<developer>_<area>`
2. 檢查分支名是否符合 §3.1
3. 依 §3.2 取得或確認 `<developer>` 名稱
4. 檢查 commit message 是否符合 §3.2，必要時用 `scripts/validate.sh commit "<message>"` 驗證
