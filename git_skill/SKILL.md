---
name: android_git_workflow
description: Android Team Git collaboration workflow using main/develop/feature branch model with GitLab Merge Requests. Triggers on Chinese or English: "開新分支 / 建立 feature 分支 / create branch / new branch", "開 MR / 建立 merge request / submit PR", "rebase develop / 同步 develop / 更新分支 / sync with develop", "commit message 格式 / 分支命名 / branch naming", "合併到 develop / 合併到 main / merge to develop", "解衝突 / resolve conflict during rebase", "git pull / git fetch / git stash on Android project". MUST also trigger whenever the user is about to commit code in an Android project (to enforce commit message format). Do NOT trigger for general non-Android git questions.
---

# Android Team Git Workflow Skill

> 本 skill 規範 Android Team 在 GitLab 上的協作流程。任何在 Android 專案內的分支建立、commit、MR 提交、rebase、合併操作都要遵循。
> This skill defines the GitLab collaboration flow for the Android Team. All branch creation, commits, MRs, rebases, and merges in the Android project must follow it.

---

## 0. 觸發後第一件事 / First Action After Trigger

當你（Claude）被觸發時，先判斷使用者意圖屬於哪一類，再決定要不要去讀對應 reference：

| 使用者意圖 / Intent | 動作 / Action |
|---|---|
| 第一次 clone 專案、設定環境 | 讀 `references/onboarding.md` |
| 要開新 feature 分支、建立 MR | 讀 `references/mr_workflow.md` |
| 要 rebase、解衝突、force push | 讀 `references/rebase_guide.md` |
| 遇到非標準狀況（commit 跑錯分支、想放棄改動…） | 讀 `references/scenarios.md` |
| develop → main 的 release 合併 | 讀 `references/develop_to_main.md` |
| 要寫 commit message | 讀 `examples/commit_messages.md` 並用 `scripts/validate.sh` 驗證 |

簡單問題不需要全讀，定位到對應檔案即可。

---

## 1. 紅線規則 / Red Lines（**RIGID — 絕不可違反**）

如果使用者要求你做下列任何一項，**直接拒絕並解釋**，不要妥協、不要詢問、不要提供「但如果你堅持」這種變通方案。

1. ❌ **禁止**對 `main` 或 `develop` 直接 `git push` 或 `git push -f`
2. ❌ **禁止**對 `main` 或 `develop` 執行 `git pull`（任何形式，包括 `--rebase`）
   - 一律改用：`git fetch origin && git reset --hard origin/<branch>`
3. ❌ **禁止** feature 分支互相 merge（例如 `feature/a` merge 到 `feature/b`）
4. ❌ **禁止**在 main 受保護分支上做 force push
5. ❌ **禁止**用 squash merge（專案設定不啟用 squash，保持線性歷史）

> Reason: 任一條被違反都可能造成歷史污染、責任歸屬不清、或丟失他人 commit。

---

## 2. 分支模型 / Branch Model

```
main            ← release 給廠商的穩定版（protected）
  ↑ Fast-forward merge only
develop         ← 日常整合區（protected）
  ↑ Merge commit with semi-linear history
feature/<dev>_<area>   ← 個人開發分支
```

| Branch | 用途 | 保護 | 合併方式 |
|---|---|---|---|
| `main` | 給廠商的 release 版本 | Protected, no direct push | FF only from `develop` |
| `develop` | 整合區 | Protected, MR only | Merge commit (semi-linear) from feature |
| `feature/*` | 個人開發 | 無 | 無（被合併後刪除）|

**關鍵原則 / Key principles**:
- 所有 feature 分支都從**最新的 develop** 開出
- feature 分支完成後**只 MR 回 develop**，不串接其他人的 feature
- MR 合併後 feature 分支**刪除**（GitLab 勾 "Delete source branch" + 本地 `git branch -d`）

---

## 3. 命名規則 / Naming Rules（**RIGID**）

### 3.1 分支名 / Branch name

格式：

```
feature/<developer>_<area-or-feature-in-english>
```

- `<developer>`：開發者英文名（小寫）
- `<area-or-feature>`：自由命名英文功能描述，可用 `-` 或 `_` 分隔

✅ 合法 / Valid:
```
feature/noah_login_api
feature/noah_frontend
feature/alice_recyclerview-adapter
feature/bob_fix-crash-on-resume
```

❌ 不合法 / Invalid:
```
feature/noah                # 缺功能描述
feature/登入                # 不可用中文
noah_login                  # 缺 feature/ 前綴
```

### 3.2 Commit message（**強制 / Mandatory**）

格式：

```
[<type>] <主要任務簡要 / brief summary> - <developer>

Add:
- ...
Fix:
- ...
```

- `<type>` 可擴充，常用：`feat` / `fix` / `refactor` / `docs` / `chore` / `test` / `style` / `perf` / `build` / `ci`
- 結尾 `- <developer>` 為**強制**，不可省略
- 第一行為標題（建議 ≤ 50 字元），下方可選擇性條列詳細變更

✅ 範例：
```
[feat] 新增登入 API 邏輯 - noah

Add:
- auth api endpoint
- login button click handler
Fix:
- token refresh edge case
```

```
[fix] 修正登入崩潰問題 - noah
```

❌ 不合法：
```
新增登入 API                       # 缺類型 tag、缺開發者
[feat] 新增登入 API                # 缺開發者
fix: 修正登入崩潰 - noah            # 類型應為 [fix] 而非 fix:
```

> 驗證工具 / Validator: `scripts/validate.sh` 可檢查分支名與 commit message。

---

## 4. Commit 時的觸發行為 / Commit-Time Behavior

當使用者執行 `git commit`、要求你產生 commit message、或貼 commit 草稿給你看時：

1. **檢查當前分支** (`git branch --show-current`):
   - 若在 `main` 或 `develop` → 警告：「禁止在 protected branch commit，請先 `git checkout -b feature/<dev>_<area>`」
   - 若分支名不符合 §3.1 格式 → 警告並建議重新命名
2. **檢查 commit message** 是否符合 §3.2 格式
3. 必要時使用 `scripts/validate.sh "<message>"` 自動驗證

---

## 5. 預設指令模板 / Default Command Templates

### 開始新功能 / Start new feature
```bash
git checkout develop
git fetch origin
git reset --hard origin/develop          # 對齊遠端，禁止用 git pull
git checkout -b feature/<dev>_<area>
```

### 同步 develop 最新狀態到 feature 分支 / Sync feature with latest develop
```bash
git checkout feature/<your-branch>
git fetch origin
git rebase origin/develop                 # 注意是 origin/develop，不是 origin develop
# 解衝突 → git add <files> → git rebase --continue
git push -f origin feature/<your-branch>
```

### MR 合併後清理 / Cleanup after MR merged
```bash
git checkout develop
git fetch origin
git reset --hard origin/develop           # 同步整合後的 develop（不要用 git pull）
git branch -d feature/<your-branch>       # 刪除本地 feature
git remote prune origin                   # 清掉已刪遠端分支的追蹤
```

> 注意：原始文件的 cleanup 步驟有寫 `git pull --rebase origin develop`，依本團隊規則應改為 `fetch + reset --hard`，與紅線規則一致。

---

## 6. 路由提示 / Routing Hints for Claude

- 使用者貼出多步驟指令請求「審查」→ 對照本檔 §1 紅線 + §3 命名 + §5 模板
- 使用者問「為什麼要 rebase 不能 merge」→ 回答：保持線性歷史、graph 乾淨、責任歸屬清晰；詳細參見 `references/rebase_guide.md`
- 使用者問「git pull 跟 fetch 差別」→ 簡答後強調本團隊規則：對 protected branch 一律 fetch + reset
- 任何違反 §1 的請求 → **直接拒絕**，並引導到對應正確流程
