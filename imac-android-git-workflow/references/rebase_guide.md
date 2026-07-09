# Rebase 與衝突處理 / Rebase & Conflict Resolution

本團隊一律使用 rebase 而非 merge 來同步 develop 的更新。理由：保持 commit 歷史線性，避免「Merge branch 'develop' into 'feature/xxx'」這類雜訊 commit，讓 GitLab graph 更易閱讀。

---

## 1. 把 feature 分支 rebase 到最新 develop

當 MR 顯示「需要更新分支」或 reviewer 要求先跟上最新 develop：

```bash
git checkout feature/<your-branch>
git fetch origin
git rebase origin/develop
```

> ⚠️ 注意是 `origin/develop`（**斜線**），不是 `origin develop`（空格）。空格版會被當作兩個 ref，行為錯誤。

---

## 2. Rebase 過程有衝突 / Conflicts during rebase

git 會停在衝突 commit。流程：

```bash
# 1. 編輯衝突檔案（找 <<<<<<<, =======, >>>>>>> 標記）
# 2. 標記為已解決
git add <conflicted-files>

# 3. 繼續 rebase
git rebase --continue

# 4. 可能會再遇到下一個衝突 commit，重複 1-3 直到完成
```

### 想放棄 rebase 回到原狀

```bash
git rebase --abort
```

---

## 3. Rebase 後 force push

rebase 會重寫 commit 歷史，所以推送時必須 `-f`（force push）：

```bash
git push -f origin feature/<your-branch>
```

> ⚠️ **只允許 force push 到自己的 feature 分支**。永遠**不可**對 `main` 或 `develop` force push（紅線規則 §1）。

---

## 4. GitLab UI 上的 Rebase 按鈕

當 MR 提示需要更新時，GitLab UI 通常會顯示「Rebase」按鈕，可直接點擊讓 GitLab 端做 rebase。如果沒有按鈕（例如有衝突），就改用本機流程。

---

## 5. 常見問題 / FAQ

### Q: 為什麼不用 `git pull --rebase` 一次搞定？

對 **feature 分支**，`git pull --rebase origin develop` 行為等同 `fetch + rebase`，技術上沒問題。但本團隊統一規範一律 `fetch + rebase` 兩步驟，原因：
- 對 `main`/`develop` 一律禁止 pull（紅線），統一指令模板降低誤用風險
- 兩步驟讓 fetch 結果可先檢視（`git log origin/develop --oneline -10`）再決定是否 rebase

### Q: 為什麼不用 merge？

merge 會產生額外的 merge commit，使 graph 在多人協作時變得很亂。rebase 把你的 commit 重新 apply 到最新 develop 上，歷史是一條乾淨的線。

### Q: rebase 後要重新跑測試嗎？

要。rebase 等於在新的 base 上重新 apply commit，行為可能改變。push 前至少跑一次本地驗證。
