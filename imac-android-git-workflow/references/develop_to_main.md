# develop → main Release 合併流程

`develop` 累積到一個 release 點時，由**有 main push 權限者**將 develop 合併到 main。

---

## 1. 合併方式 / Merge method

**Fast-forward only**。GitLab 專案層級已設定 main 的 merge method 為 FF。

理由：main 必須保持完全線性歷史，每個版本點都直接對應 develop 的某個 commit，不允許產生 merge commit。

---

## 2. 前置條件 / Preconditions

合併前確認：

1. `develop` 在最近一次整合後，所有預定進入此 release 的 MR 都已合併
2. CI 在 develop 上全綠（lint / unit test / build）
3. QA / 廠商驗收回饋已處理完
4. 已有 release notes 草稿（commit message 整理）

---

## 3. 操作步驟 / Steps

### 3.1 對齊本地 develop 與 main

```bash
git fetch origin

git checkout develop
git reset --hard origin/develop

git checkout main
git reset --hard origin/main
```

### 3.2 確認 develop 領先 main 的範圍

```bash
git log --oneline origin/main..origin/develop
```

確認所有 commit 都是預期要 release 的內容。

### 3.3 建立 MR (develop → main)

到 GitLab：

- **Source**: `develop`
- **Target**: `main`
- **Title**: `Release vX.Y.Z`（或團隊定義的 release tag）
- **Description**: 列出本次 release 的主要功能、修正、已知問題

> 注意：因為是 FF merge，develop 必須能直接 fast-forward 到 main。如果 main 上有 develop 沒有的 commit（例如有人手動 hotfix），FF 會失敗——此時需先把 main 上那些 commit cherry-pick / merge 回 develop，再重做。

### 3.4 合併

由有權限者點 Merge。FF merge 不產生新 commit，main 的 HEAD 直接前進到 develop 的 HEAD。

### 3.5 打 tag（建議）

```bash
git fetch origin
git checkout main
git reset --hard origin/main
git tag -a v<X.Y.Z> -m "Release v<X.Y.Z>"
git push origin v<X.Y.Z>
```

---

## 4. 紅線提醒 / Red Lines Reminder

即使是 release 合併，仍受 `SKILL.md` §1 紅線規範：

- ❌ 不可對 main 直接 push（必須走 MR）
- ❌ 不可對 main force push
- ❌ 不可在 main 執行 `git pull`（一律 `fetch + reset --hard`）

---

## 5. Hotfix 例外處理（補充）

如果 main 上發現緊急 bug 需 hotfix，且不適合等下一次 release：

1. 從 main 開 `hotfix/<name>` 分支
2. 修完 → MR 回 main（也走 FF；此時 hotfix 必須 rebase 到 main 之上）
3. **同時** cherry-pick 或 merge 回 develop，避免 hotfix 在下次 release 時被 develop 覆蓋

> 這流程不在原始文件內，僅作為補充建議。實際是否採用 hotfix 模式請與 team 確認。
