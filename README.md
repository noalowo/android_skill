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

安裝後，Claude Code 會自動在處理 Android 相關任務時載入此技能，包含以下情境：

- 新增 Activity / 頁面 / 模組
- 建立 MVP 架構
- 新增 Retrofit API 端點
- 建立 RecyclerView Adapter
- 修改 AndroidManifest.xml

### 方法二：手動參考

直接查閱各目錄下的範本與參考文件：

1. 建立新模組時，參考 `SKILL.md` 的 MVP 架構範本
2. 複製 `examples/` 下對應的 Java 範例，替換 `Sample` 為實際模組名稱
3. 使用腳本快速建立檔案結構：

```bash
# 建立 MVP 模組
bash scripts/create_mvp_module.sh app/src/main/java/com/example/app ui/settings Settings

# 檢查模組完整性
bash scripts/check_mvp_module.sh app/src/main/java/com/example/app/ui/settings Settings
```

## 規範重點

- Java 類別使用 PascalCase，變數與方法使用 camelCase
- XML View ID 使用 snake_case 加元件前綴（如 `iv_`、`btn_`、`tv_`）
- JSON 欄位使用 `@SerializedName` 映射至 camelCase 變數
- GET/DELETE 帶 body 時須使用 `@HTTP` 而非 `@GET`/`@DELETE`
- Presenter 使用 WeakReference 持有 View 避免記憶體洩漏
