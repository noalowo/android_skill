# MR/PR 完整流程

從開分支到合併、清理，再到 develop → main release 的標準流程。

---

## 1. 從最新 develop 開 feature 分支

```bash
git checkout develop
git fetch origin
git reset --hard origin/develop
git checkout -b feature/<developer>_<area>
```

不要用 `git pull` 對齊 develop，理由見 `SKILL.md` §4「同步 protected 分支」。

分支命名格式參見 `SKILL.md` §3.1。

---

## 2. 開發與提交

按功能單元做 commit，每個 commit 都要符合 `SKILL.md` §3.2 的格式：

```
[<type>] <簡述> - <developer>
```

---

## 3. 首次推送並建立 MR/PR

### 3.1 首次推送

```bash
git push -u origin feature/<developer>_<area>
```

`-u` 設定 upstream，之後可直接 `git push`。

### 3.2 建立 MR/PR

- **Source**：`feature/<developer>_<area>`
- **Target**：`develop`
- 標題沿用 commit message 標題格式：`[<type>] <簡述> - <developer>`
- 說明欄視情況補充 Add / Fix / Refactor 條列、測試方式、風險點
- 建立時勾選來源分支合併後自動刪除（見 `SKILL.md` §5）

---

## 4. MR/PR 後續變更

MR/PR 建立後，所有後續變更（review 修正、解衝突、加 commit、rebase 對齊最新 develop）：

1. 在 feature 分支上新增 commit，或 rebase 到 `origin/develop`（見 `references/troubleshooting.md`）
2. rebase 後推送到同一個遠端分支，需要 `git push -f`（只限自己的 feature 分支，見紅線 1）
3. 推送後平台會自動更新 MR/PR

---

## 5. 合併後本地清理

MR/PR 合併（由有權限者操作）後：

```bash
git checkout develop
git fetch origin
git reset --hard origin/develop
git branch -d feature/<developer>_<area>
git remote prune origin
```

清理完成後回到步驟 1，從乾淨的 develop 開下一個 feature 分支。

---

## 6. develop → main 的 release 流程

### 6.1 前置條件

- `develop` 上所有預定進入此 release 的 MR/PR 都已合併
- CI 在 `develop` 上全綠（lint / unit test / build）
- QA 驗收回饋已處理完

### 6.2 對齊本地分支

```bash
git fetch origin
git checkout develop
git reset --hard origin/develop
git checkout main
git reset --hard origin/main
```

### 6.3 確認 release 範圍

```bash
git log --oneline origin/main..origin/develop
```

確認列出的 commit 都是預期要 release 的內容。

### 6.4 建立 MR/PR

- **Source**：`develop`
- **Target**：`main`
- **標題**：`Release vX.Y.Z`
- **說明**：列出本次 release 的主要功能、修正、已知問題

### 6.5 合併與打 tag

由有權限者合併，產生一個 merge commit。合併後：

```bash
git fetch origin
git checkout main
git reset --hard origin/main
git tag -a v<X.Y.Z> -m "Release v<X.Y.Z>"
git push origin v<X.Y.Z>
```

即使是 release 合併，仍受 `SKILL.md` §1 紅線規範：不可對 `main` 直接 push，只能走 MR/PR。
