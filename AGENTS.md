# imac Android 團隊開發規範（AGENTS.md）

給讀取 `AGENTS.md` 的 agent（Codex、Antigravity、Gemini 等）使用的常駐精簡規範。
完整內容以 `skills/<name>/SKILL.md` 為準；本檔是對齊的摘要，衝突時以 skill 為準。
適用對象：Java Android 專案。動手前先讀專案自己的 `CLAUDE.md`（若存在）。

## 1. 命名（imac-android-architecture-skill）

- Java 類別 PascalCase；變數 / 方法 camelCase；常數（static final）UPPER_SNAKE_CASE。
- XML 檔名 snake_case；View ID 用「元件前綴 + snake_case」（`iv_`、`tv_`、`btn_`、`rv_`、`et_`、`cl_`、`ll_`…），Java 端對應變數保留前綴 camelCase（`ivBack`、`btnSubmit`）。
- API JSON 欄位 snake_case；Java 端用 camelCase + `@SerializedName`。
- Package 目錄全小寫（可用底線）。

## 2. 架構（imac-android-architecture-skill）

- 頁面 / 模組採 MVP：每個模組要有 `XxxContract` / `XxxPresenter` / `XxxActivity`（或 Fragment）。Presenter 不依賴 Android framework 型別，方便寫單元測試。
- 用 ViewBinding，不用 findViewById。
- 打 API 用 Retrofit；大型資料的 Intent 傳遞走既有規範。
- Base 類別、範本、Adapter / Presenter 測試範例在 `skills/imac-android-architecture-skill/`（`examples/`、`assets/`、`scripts/`）。

## 3. Git 協作（imac-android-git-workflow）— 紅線規則

- **禁止**對 `main`、`develop` 直接 push / force push；**禁止** feature 分支互相 merge，一律開 feature 分支。
- 支援 GitLab 與 GitHub（MR/PR 皆可）。分支命名：`feature/<dev>_<area>`（例：`feature/noah_login`），分支固定用 `develop`（不是 `dev`）。
- commit message 格式：`[type] 主旨 - <name>`，type ∈ `feat / fix / refactor / docs / chore / test / style / perf / build / ci`（例：`[feat] 新增登入頁 - noah`）。
- 同步 `develop` / `main` 用 `git fetch origin && git reset --hard origin/<branch>`，不用 `git pull`。
- 合流一律走 MR/PR；develop → main 產生 merge commit 後在 main 打 tag。詳細情境見 `skills/imac-android-git-workflow/references/workflow.md`、`references/troubleshooting.md`。

## 4. CI（imac-android-ci-pipeline）

- GitLab Free tier，四階段：`lint → sast → test → build`。
  - lint：Checkstyle + Android Lint
  - sast：SpotBugs + find-sec-bugs（靜態安全檢測）
  - test：JUnit + JaCoCo（含覆蓋率）
  - build：`assembleDebug`（debug APK artifact）
- 不含 CD（部署 / 發佈 / release 簽名）。`.gitlab-ci.yml` 與規則檔在 `skills/imac-android-ci-pipeline/assets/`。
