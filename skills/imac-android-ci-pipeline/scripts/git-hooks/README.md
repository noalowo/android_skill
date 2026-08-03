# 本地 git hooks

`.git/hooks/` 不會被 git 追蹤，需各自安裝。

## 安裝

由 Claude 執行安裝，以 `${CLAUDE_PLUGIN_ROOT}/skills/imac-android-ci-pipeline/scripts/git-hooks/` 作為來源，把 hook 複製到專案的 `.git/hooks/` 並加上執行權限：

```sh
cp "${CLAUDE_PLUGIN_ROOT}/skills/imac-android-ci-pipeline/scripts/git-hooks/pre-commit" .git/hooks/pre-commit
cp "${CLAUDE_PLUGIN_ROOT}/skills/imac-android-ci-pipeline/scripts/git-hooks/pre-push"   .git/hooks/pre-push
chmod +x .git/hooks/pre-commit .git/hooks/pre-push
```

## 行為

- `pre-commit`：commit 前跑 Checkstyle + SpotBugs（快、style + 安全）
- `pre-push`：push 前跑 Android Lint + 單元測試（慢）

## 緊急 bypass

`git commit --no-verify` / `git push --no-verify` 可跳過。團隊應約定只在緊急情況使用。

## Windows

git hook 走 git bash，`#!/bin/sh` 可運作，hook 檔不需 chmod。
