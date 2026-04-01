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

- **SKILL.md** - 命名規則、MVP 架構範本、Activity 生命週期規範、Retrofit 規範、大型資料 Intent 傳遞、建立模組後的檢查清單
- **assets/** - 可直接複製使用的 XML 範本
- **examples/** - 完整的 Java 範例程式碼，替換模組名稱即可使用
- **references/** - 快速查閱的 API 對照表
- **scripts/** - 自動化建立與驗證 MVP 模組結構的 shell 腳本

## 如何在 Android Studio 中使用

### 終端 Claude Code 使用者

將此專案複製至 Claude Code 的 skills 目錄，以下兩種方式：
1. download zip 然後解壓縮到 C:\User\User\.claude\skills\ 目錄底下
2. 打開 PowerShell 進入自己的 C:\User\User\.claude\skills\ 並把此專案git clone到裡面(建議)

如何確認：

    1. 終端輸入 claude
    
    2. 在 claude code 輸入 /skills 
       -> claude code 會顯示 project 層級的 skills & user 層級的 skills

### 終端 Gemini CLI 使用者

將此專案複製至 Gemini CLI 的 skills 目錄，以下兩種方式：
1. download zip 然後解壓縮到 C:\User\User\.gemini\skills\ 目錄底下
2. 打開 PowerShell 進入自己的 C:\User\User\.gemini\skills\ 並把此專案git clone到裡面(建議)

如何確認：

    1. 終端輸入 gemini   or  gemini skills list

    2. 在 gemini cli 輸入 /skills list
       -> gemini cli 會顯示 skills   (如使用 gemini skills list 會顯示你的 skills 放在哪些資料夾)

### Android Studio Plugin GitHub Copilot 使用者

先確認有無開啟skills：進到 setting -> 點 Tools -> GitHub Copilot -> Chat -> 找到 Enable Skills
<img width="980" height="724" alt="image" src="https://github.com/user-attachments/assets/7bf29900-f882-48e8-b441-9b9a0d41a9ca" />

下面是 GitHub Copilot skills 官方文件，內容(如下圖)提到專案skills和個人skills會讀取那些資料夾：
https://docs.github.com/en/copilot/concepts/agents/about-agent-skills
<img width="785" height="363" alt="image" src="https://github.com/user-attachments/assets/0da8a7f9-cdab-4e7a-ac3f-315951f5abbd" />

## 安裝後，AI 會自動在處理 Android 相關任務時載入此技能，包含以下情境：

- 新增 Activity / 頁面 / 模組
- 建立 MVP 架構
- 新增 Retrofit API 端點
- 建立 RecyclerView Adapter
- 修改 AndroidManifest.xml
- 處理 Activity 生命週期（onResume/onPause/onSaveInstanceState 等）

## 如何取得最新版本

此 skill 持續更新中。若要同步最新內容：

```bash
# 進入安裝目錄後執行
git pull origin main
```

p.s.
