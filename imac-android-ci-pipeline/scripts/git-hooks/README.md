# 本地 git hooks

`.git/hooks/` 不會被 git 追蹤，需各自安裝。

## 安裝

從專案根目錄：

```sh
cp skills/android_skill/ci/scripts/git-hooks/pre-commit .git/hooks/pre-commit
cp skills/android_skill/ci/scripts/git-hooks/pre-push   .git/hooks/pre-push
chmod +x .git/hooks/pre-commit .git/hooks/pre-push
```

（實際來源路徑依本 skill 在專案中的位置調整。）

## 行為

- `pre-commit`：commit 前跑 Checkstyle + SpotBugs（快、style + 安全）
- `pre-push`：push 前跑 Android Lint + 單元測試（慢）

## 緊急 bypass

`git commit --no-verify` / `git push --no-verify` 可跳過。團隊應約定只在緊急情況使用。

## Windows

git hook 走 git bash，`#!/bin/sh` 可運作，hook 檔不需 chmod。
