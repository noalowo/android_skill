# imac-android skills

台中科大 imac 社群的 Android 團隊開發規範，打包成 **Claude Code / Codex CLI plugin**。
內含三個 skill，AI 在處理 Java Android 任務時會自動載入：

| Skill | 內容 |
|-------|------|
| **imac-android-architecture-skill** | MVP 架構、命名規則、Activity 生命週期、ViewBinding、Retrofit、RecyclerView Adapter、Presenter 單元測試 |
| **imac-android-ci-pipeline** | GitLab CI（Free tier）四階段：lint / SAST / 單元測試 / build debug APK（不含 CD） |
| **imac-android-git-workflow** | main/develop/feature 分支模型、commit 格式、分支命名、rebase、MR/PR、release 合併流程（GitLab 與 GitHub 皆適用） |

> 適用對象：Java Android 專案。各 skill 執行前會先讀專案自己的 `CLAUDE.md`（若有）。

---

## 安裝

### Claude Code（推薦：Plugin Marketplace）

在 Claude Code 內執行兩行指令即可安裝，之後用 `/plugin` 更新：

```
/plugin marketplace add noalowo/android_skill
/plugin install imac-android@imac-skills
```

- 驗證：`/plugin` 看到 `imac-android@imac-skills` 為 enabled，或 `/skills` 列出三個 skill。
- 更新：`/plugin marketplace update imac-skills`。

### Claude Code（手動複製）

把本專案 `skills/` 底下的三個目錄複製到你的 skills 目錄（三個 skill 目錄需各自位於 skills 根層，AI 不會遞迴掃描）：

```
C:/Users/<username>/.claude/skills/
├── imac-android-architecture-skill
├── imac-android-ci-pipeline
└── imac-android-git-workflow
```

方式：下載 zip 解壓、或 git clone 後把 `skills/` 底下三個目錄搬過去。
驗證：終端輸入 `claude`，再輸入 `/skills`，會列出 project 層級與 user 層級的 skills。

### Codex CLI

- **Plugin（推薦）**：Codex 讀 `.codex-plugin/plugin.json`，會載入同一批 `skills/`。

  ```
  codex plugin marketplace add noalowo/android_skill
  codex plugin install imac-android
  ```

  （實際子指令以你的 Codex 版本為準，可 `codex plugin --help` 確認。）

- **Instruction-tier（fallback）**：把本專案的 `AGENTS.md` 複製到你的 Android 專案根目錄，或放 `~/.codex/AGENTS.md` 當全域規範。

### Antigravity

Antigravity 沒有 plugin 系統，走 instruction 檔：把本專案的 `AGENTS.md` 複製到 Android 專案根目錄，Antigravity 會當成 always-on rules（也可放進 `.agent/rules/`）。全域規範放 `~/.gemini/AGENTS.md`。

### Gemini CLI

把 `skills/` 底下三個目錄複製到 `C:/Users/<username>/.gemini/skills/`（結構同 Claude Code 手動複製）。
驗證：`gemini skills list`，或在 Gemini CLI 內 `/skills list`。
只吃 instruction 的情境也可改用 `AGENTS.md`。

### GitHub Copilot

先確認已 Enable Skills：

![enable skills](https://github.com/noalowo/image/blob/main/%E8%9E%A2%E5%B9%95%E6%93%B7%E5%8F%96%E7%95%AB%E9%9D%A2%202026-04-01%20151246.png)

Copilot 如何讀取專案 / 個人 Skills，見官方文件：
https://docs.github.com/en/copilot/concepts/agents/about-agent-skills

![copilot skills doc](https://github.com/noalowo/image/blob/main/%E8%9E%A2%E5%B9%95%E6%93%B7%E5%8F%96%E7%95%AB%E9%9D%A2%202026-04-01%20152042.png)

---

## Slash 指令與自我測試

安裝 plugin 後，除了自動觸發的三個 skill，另提供手動 slash 指令（包裝既有腳本，給明確入口）：

| 指令 | 作用 | 參數 |
|------|------|------|
| `/imac-validate` | 驗證目前分支名與最後一筆 commit message 是否符規範 | 留空自動檢查；或 `branch <name>` / `commit "<msg>"` |
| `/imac-check-mvp` | 檢查一個 MVP 模組結構是否完整 | `<module_dir> <ModuleName>` |
| `/imac-new-mvp` | 建立 MVP 模組骨架檔案 | `<package_path> <module_name> <ModuleName> <full_package>` |

維護此 repo 時，`bash check_skills.sh` 會檢查每個 `skills/*/SKILL.md` 的 frontmatter 能否被正確解析（開頭、`name` 與目錄一致、必要欄位、避免會讓 YAML 失敗的未加引號冒號空格），提交前跑一次可避免 skill 載入失敗。

---

## 自動觸發情境

安裝後，AI 處理以下 Android 任務時會自動載入對應 skill：

- **架構（imac-android-architecture-skill）**：新增 Activity / 頁面 / 模組、建立 MVP、加 Retrofit API、建 RecyclerView Adapter、改 AndroidManifest、處理生命週期（onResume/onPause/onSaveInstanceState）。
- **CI（imac-android-ci-pipeline）**：設定 GitLab CI、建 `.gitlab-ci.yml`、pipeline（lint/sast/test/build）、Checkstyle、SpotBugs + find-sec-bugs、JUnit + JaCoCo、build debug APK、pre-commit hook。
- **Git（imac-android-git-workflow）**：開 feature 分支 / 建 MR 或 PR、撰寫或檢查 commit message、rebase / 同步 develop、解衝突、合併到 develop / release 到 main。

---

## 目錄結構

```
android_skill/
├── README.md
├── LICENSE
├── AGENTS.md                                  # Codex / Antigravity / Gemini 等 instruction-tier 常駐規範
├── check_skills.sh                            # 驗證各 SKILL.md frontmatter 的自我測試
├── .claude-plugin/                            # Claude Code plugin / marketplace 設定
│   ├── marketplace.json
│   └── plugin.json
├── .codex-plugin/                             # Codex CLI plugin 設定（skills 指向 ./skills/）
│   └── plugin.json
├── commands/                                  # slash 指令
│   ├── imac-validate.md
│   ├── imac-check-mvp.md
│   └── imac-new-mvp.md
└── skills/                                    # 三個 skill，Claude Code / Codex 皆自動掃描
    ├── imac-android-architecture-skill/       # SKILL.md + assets / examples / references / scripts
    ├── imac-android-ci-pipeline/              # SKILL.md + assets / references / scripts(git-hooks)
    └── imac-android-git-workflow/             # SKILL.md + examples / references / scripts
```

各 skill 的 `references/` 收快速查閱的對照表與分情境指南，`examples/` 收可直接替換模組名使用的範例，`assets/` 收可複製的範本檔（layout XML、`.gitlab-ci.yml`、checkstyle 規則等）。

---

p.s. 此 skill 持續更新中。
