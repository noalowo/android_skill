# Rebase 與常見狀況處理

本團隊一律使用 rebase 而非 merge 來同步 develop 的更新，保持 commit 歷史線性。當使用者描述的狀況符合下列任一情境時，照對應步驟處理。

---

## 1. 把 feature 分支 rebase 到最新 develop

當 MR/PR 顯示需要更新分支，或 reviewer 要求先跟上最新 develop：

```bash
git checkout feature/<your-branch>
git fetch origin
git rebase origin/develop
```

注意是 `origin/develop`（斜線），不是 `origin develop`（空格）。空格版會被當作兩個 ref，行為錯誤。

---

## 2. Rebase 過程有衝突

git 會停在衝突的 commit：

```bash
# 1. 編輯衝突檔案（找 <<<<<<<, =======, >>>>>>> 標記）
# 2. 標記為已解決
git add <conflicted-files>

# 3. 繼續 rebase
git rebase --continue

# 4. 可能再遇到下一個衝突 commit，重複 1-3 直到完成
```

想放棄 rebase、回到開始前的狀態：

```bash
git rebase --abort
```

---

## 3. Rebase 後 force push

rebase 會重寫 commit 歷史，推送時必須 `-f`：

```bash
git push -f origin feature/<your-branch>
```

只允許 force push 到自己的 feature 分支。永遠不可對 `main` 或 `develop` force push（紅線 1）。

---

## 4. 平台 UI 的 rebase / 更新按鈕

MR/PR 顯示需要更新時，平台通常提供一個按鈕可直接在遠端做 rebase：GitLab 的 Rebase 按鈕、GitHub 的 Update branch 按鈕。如果沒有按鈕，或按下後仍有衝突，改用本機流程（見 §1、§2）。

---

## 5. 不小心 commit 到 develop，想搬到 feature 分支

前提：尚未 push 到遠端。如果已經 push 到 develop，因為 develop 是 protected branch，正常情況也 push 不上去；若真的發生，需聯絡有權限者處理。

```bash
# 1. 確認要搬的 commit hash
git log --oneline

# 2. 切到（或建立）feature 分支，cherry-pick
git checkout feature/<your-branch>        # 或 git checkout -b feature/<developer>_<area>
git cherry-pick <commit-hash>

# 3. 清掉 develop 上的錯誤 commit
git checkout develop
git fetch origin
git reset --hard origin/develop
```

---

## 6. 想放棄本機改動，對齊遠端

```bash
git fetch origin
git reset --hard origin/<branch-name>
```

這會丟掉所有未 push 的本機改動，無法還原。執行前先 `git status` 確認，並跟使用者確認真的要丟。

---

## 7. 開發到一半需要切換分支

```bash
# 暫存當前未 commit 改動
git stash

# 切到別的分支處理事情
git checkout other-branch
# ...

# 回來原本分支恢復改動
git checkout feature/<your-branch>
git stash pop
```

如果有多份 stash，`git stash list` 查看，`git stash pop stash@{n}` 取特定一份。

---

## 8. 多人共用同一 feature 分支

一般不建議多人改同一 feature 分支。若不得已：

```bash
git fetch origin
git rebase origin/feature/<your-branch>    # 把對方的更動 rebase 進來
# 解衝突...
git push -f origin feature/<your-branch>
```

如果本地已有新 commit 而對方剛 push 新 commit，force push 會覆蓋對方的內容。務必先 fetch + rebase 再 push。

---

## 9. FAQ

### 為什麼用 rebase 不用 merge

merge 會產生額外的 merge commit，多人協作時 graph 會變得很亂。rebase 把 commit 重新 apply 到最新 develop 上，歷史是一條乾淨的線。

### 為什麼 protected 分支不用 git pull

本地 `develop`（或 `main`）若累積雜 commit，`git pull` 會產生 merge commit，並被之後開出的 feature 分支繼承進 MR/PR；一律改用 `git fetch origin && git reset --hard origin/<branch>` 直接對齊遠端。

### rebase 後要重新跑測試嗎

要。rebase 等於在新的 base 上重新 apply commit，行為可能改變，push 前至少跑一次本地驗證。
