# 新成員初次設定 / First-Time Setup

新成員加入 Android 專案後，第一次設定環境的步驟。

## 1. Clone 專案 / Clone the project

```bash
git clone <gitlab-repo-url>
cd <project-folder>
```

## 2. 設定本地 Git 身份 / Configure local git identity

```bash
git config user.name "<your-name>"
git config user.email "<your-email>"
```

> 建議用 `--local` 而非 `--global`，避免影響其他專案。

## 3. 確認分支狀態 / Verify branches

```bash
git branch -a                  # 查看所有分支（含遠端）
git checkout develop           # 切到 develop
git fetch origin
git reset --hard origin/develop
```

## 4. 確認你不會誤推 / Verify you won't push by accident

確認你**沒有** `main` 與 `develop` 的 push 權限（GitLab 應該已設為 protected）。如果可以直接 push，請聯絡管理員調整權限。

## 5. 安裝 commit message hook（可選但建議）

把 `scripts/validate.sh` 連到 `.git/hooks/commit-msg` 上，可在每次 commit 時自動驗證格式：

```bash
# 從專案根目錄
ln -s ../../skills/android_skill/git_skill/scripts/validate.sh .git/hooks/commit-msg
chmod +x .git/hooks/commit-msg
```

Windows (Git Bash):
```bash
cp skills/android_skill/git_skill/scripts/validate.sh .git/hooks/commit-msg
chmod +x .git/hooks/commit-msg
```

## 6. 開始第一個 feature 分支

```bash
git checkout -b feature/<your-name>_<feature-area>
```

範例：`feature/noah_setup-onboarding`

接下來流程詳見 `mr_workflow.md`。
