# android_skill

Android Java 專案的 MVP 開發規範技能，提供命名規則、架構範本、Retrofit API 規範及實用腳本。

## 目錄結構

```
android_skill/
├── SKILL.md                          # 完整開發規範（主要參考文件）
├── assets/
│   ├── layout_template.xml           # Layout XML 範本
│   └── manifest_activity_template.xml # AndroidManifest Activity 註冊範本
├── examples/
│   ├── MvpActivityExample.java       # MVP Activity 範例
│   ├── MvpContractExample.java       # MVP Contract 範例
│   ├── MvpPresenterExample.java      # MVP Presenter 範例
│   ├── RetrofitApiExample.java       # Retrofit ApiService 範例
│   └── SerializedNameExample.java    # Gson @SerializedName 範例
├── references/
│   ├── immersive-mode.md             # Android Immersive Mode API 參考
│   ├── retrofit-annotations.md       # Retrofit2 常用註解參考
│   └── view-id-prefixes.md           # View ID 前綴命名規範
└── scripts/
    ├── create_mvp_module.sh          # 快速建立 MVP 模組目錄與空白檔案
    └── check_mvp_module.sh           # 檢查 MVP 模組是否完整
```

## 內容說明

- **SKILL.md** - 命名規則、MVP 架構範本、Retrofit 規範、大型資料 Intent 傳遞、建立模組後的檢查清單
- **assets/** - 可直接複製使用的 XML 範本
- **examples/** - 完整的 Java 範例程式碼，替換模組名稱即可使用
- **references/** - 快速查閱的 API 對照表
- **scripts/** - 自動化建立與驗證 MVP 模組結構的 shell 腳本

## 如何使用

### 方法一：作為 Claude Code Skill 安裝

將此專案複製至 Claude Code 的 skills 目錄：

```bash
git clone https://github.com/noalowo/android_skill.git ~/.claude/skills/android-skill
```

如何確認：
    1. 終端輸入 claude
    2. 在 claude code 輸入 /skills 
       -> claude code 會顯示 project 層級的 skills & user 層級的 skills

### 方法二：作為 Gemini CLI Skill 安裝

將此專案複製至 Gemini CLI 的 skills 目錄：

```bash
git clone https://github.com/noalowo/android_skill.git ~/.gemini/skills/android-skill
```

如何確認：
    1. 終端輸入 gemini   or  gemini skills list
    2. 在 gemini cli 輸入 /skills list
       -> gemini cli 會顯示 skills   (如使用 gemini skills list 會顯示你的 skills 放在哪些資料夾)

### 方法三：安裝在單一專案(以各ai官方文件為主，下面提供簡易通用版)
將此專案複製至專案內的 .agent(看個人使用甚麼ai，下列使用.agent通用說明，詳細去官方文件找尋相關 skills 安裝流程)/skills 目錄：

```bash
git clone https://github.com/noalowo/android_skill.git ~/你的專案/.agent/skills/android-skill
```
copilot 或在 android studio 用 gemini api key 的使用者須在專案內 /.agent 底下新增 AGENT.md ，並輸入下列 Rules：
1. 在回覆前請先讀取並套用以下檔案：
1) `.agent/AGENTS.md`
2) `.agent/skills/android_skill/SKILL.md`

下面是github copilot skills 官方文件：
https://docs.github.com/en/copilot/concepts/agents/about-agent-skills

## 安裝後，AI 會自動在處理 Android 相關任務時載入此技能，包含以下情境：

- 新增 Activity / 頁面 / 模組
- 建立 MVP 架構
- 新增 Retrofit API 端點
- 建立 RecyclerView Adapter
- 修改 AndroidManifest.xml
