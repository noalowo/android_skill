# 常見情境處理 / Common Scenarios

當使用者描述的狀況符合下列任一情境，照對應步驟處理。

---

## 情境 1：MR 顯示無法合併 / 需要更新分支

**首選**：在 GitLab UI 直接點「Rebase」按鈕。

**沒按鈕時**（例如有衝突）：本機操作

```bash
git checkout feature/<your-branch>
git fetch origin
git rebase origin/develop
# 解衝突...
git push -f origin feature/<your-branch>
```

詳見 `rebase_guide.md`。

---

## 情境 2：Rebase 做到一半想放棄

```bash
git rebase --abort
```

回到 rebase 開始前的狀態。

---

## 情境 3：不小心 commit 到 develop，想搬到 feature 分支

⚠️ 前提：尚未 push 到遠端。如果已經 push 到 develop，請聯絡 admin（因為 develop 是 protected branch，正常情況也 push 不上去）。

```bash
# 1. 確認要搬的 commit hash
git log --oneline

# 2. 切到（或建立）feature 分支，cherry-pick
git checkout feature/<your-branch>        # 或 git checkout -b feature/<dev>_<area>
git cherry-pick <commit-hash>

# 3. 清掉 develop 上的錯誤 commit
git checkout develop
git fetch origin
git reset --hard origin/develop
```

---

## 情境 4：開發到一半需要緊急切換分支

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

## 情境 5：想完全放棄本機改動，對齊遠端

```bash
git fetch origin
git reset --hard origin/<branch-name>
```

⚠️ 這會**丟掉所有未 push 的本機改動**，無法還原。執行前確認真的要丟。

---

## ex 情境 A：什麼時候可以用 `git pull --rebase`

依本團隊規則：

- ❌ 對 `main` / `develop`：**禁止**（任何形式的 pull 都禁止）
- ⚠️ 對 feature 分支：技術上可用，但本團隊統一指令模板為 `fetch + rebase`，**建議不用** `pull --rebase`，避免習慣帶到 protected branch

詳見 `rebase_guide.md` §5 FAQ。

---

## ex 情境 B：把 feature 分支 rebase 到 develop（指令版）

```bash
git checkout feature/<your-branch>
git fetch origin
git rebase origin/develop                  # ⚠️ 是 origin/develop（斜線），不是 origin develop
```

---

## 情境 6：被推到的 feature 分支被別人也改了（多人協作同一分支）

> 一般不建議多人改同一 feature 分支。若不得已：

```bash
git fetch origin
git rebase origin/feature/<your-branch>    # 把對方的更動 rebase 進來
# 解衝突...
git push -f origin feature/<your-branch>
```

如果你已經有本地 commit 而對方剛 push 新 commit，force push 會覆蓋對方的。**先 fetch + rebase 再 push**。
