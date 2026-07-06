# android_skill

## 目錄結構

```
android_skill/
├── README.md
├── imac-android-architecture-skill/       # MVP 架構開發規範
│   ├── SKILL.md                           # 完整開發規範（主要參考文件）
│   ├── assets/
│   │   ├── layout_template.xml            # Layout XML 範本
│   │   └── manifest_activity_template.xml # AndroidManifest Activity 註冊範本
│   ├── examples/
│   │   ├── BaseActivity.java              # Base Activity 抽象類別
│   │   ├── BaseContract.java             # Base MVP Contract 介面
│   │   ├── BaseFragment.java             # Base Fragment 抽象類別
│   │   ├── BasePresenter.java            # Base Presenter 抽象類別
│   │   ├── CallbackWrapper.java          # Retrofit Callback 封裝
│   │   ├── ErrorType.java                # 錯誤類型列舉
│   │   ├── MvpActivityExample.java       # MVP Activity 範例
│   │   ├── MvpContractExample.java       # MVP Contract 範例
│   │   ├── MvpPresenterExample.java      # MVP Presenter 範例
│   │   ├── RetrofitApiExample.java       # Retrofit ApiService 範例
│   │   ├── SampleAdapterExample.java     # RecyclerView Adapter 範例
│   │   ├── SamplePresenterTest.java      # Presenter 單元測試範例
│   │   ├── SampleRepository.java         # Repository 範例
│   │   └── SerializedNameExample.java    # Gson @SerializedName 範例
│   ├── references/
│   │   ├── immersive-mode.md              # Android Immersive Mode API 參考
│   │   ├── retrofit-annotations.md        # Retrofit2 常用註解參考
│   │   └── view-id-prefixes.md            # View ID 前綴命名規範
│   └── scripts/
│       ├── create_mvp_module.sh           # 快速建立 MVP 模組目錄與空白檔案
│       └── check_mvp_module.sh            # 檢查 MVP 模組是否完整
├── imac-android-ci-pipeline/              # Android CI pipeline 規範（GitLab，Java）
│   ├── SKILL.md                           # CI pipeline 設定指南（主要參考文件）
│   ├── assets/
│   │   ├── gitlab-ci.yml                  # .gitlab-ci.yml 範本
│   │   ├── checkstyle.xml                 # Checkstyle 規則檔
│   │   ├── spotbugs-exclude.xml           # SpotBugs 排除規則
│   │   └── editorconfig                   # .editorconfig 範本
│   ├── references/
│   │   ├── version-check.md               # 使用時用 web 查證相容版本
│   │   ├── gradle-setup.md                # Android module 接 Checkstyle/SpotBugs/JaCoCo
│   │   ├── gitlab-setup.md                # 分支保護、MR 守門、Free tier SAST
│   │   ├── android-studio.md              # IDE 外掛與規則檔對齊
│   │   ├── troubleshooting.md             # 常見問題與品質門檻收緊
│   │   └── limitations.md                 # 適用前提與版本相容性
│   └── scripts/
│       └── git-hooks/
│           ├── pre-commit                 # commit 前跑 checkstyle + spotbugs
│           ├── pre-push                   # push 前跑 lint + unit test
│           └── README.md                  # git hook 安裝說明
└── git_skill/                             # Android Team Git 協作規範
    ├── SKILL.md                           # 分支模型、命名規則、紅線規則、指令模板
    ├── examples/
    │   └── commit_messages.md             # Commit message 格式範例
    ├── references/
    │   ├── develop_to_main.md             # develop → main release 合併流程
    │   ├── mr_workflow.md                 # 開 feature 分支與 MR 流程
    │   ├── onboarding.md                  # 第一次 clone 專案、設定環境
    │   ├── rebase_guide.md                # rebase、解衝突、force push
    │   └── scenarios.md                   # 非標準狀況（commit 跑錯分支、放棄改動等）
    └── scripts/
        └── validate.sh                    # 驗證分支名與 commit message
```

## 內容說明

- **imac-android-architecture-skill/SKILL.md** - 命名規則、MVP 架構範本、Activity 生命週期規範、Retrofit 規範、大型資料 Intent 傳遞、建立模組後的檢查清單
- **imac-android-architecture-skill/assets/** - 可直接複製使用的 XML 範本
- **imac-android-architecture-skill/examples/** - 完整的 Java 範例程式碼，包含 Base 類別、Repository、Adapter、Presenter 測試等，替換模組名稱即可使用
- **imac-android-architecture-skill/references/** - 快速查閱的 API 對照表
- **imac-android-architecture-skill/scripts/** - 自動化建立與驗證 MVP 模組結構的 shell 腳本
- **imac-android-ci-pipeline/SKILL.md** - Android（Java）在 GitLab Free tier 的 CI pipeline 四階段設定（lint / sast / test / build）：Checkstyle 程式碼品質、find-sec-bugs（SpotBugs）SAST、JUnit + JaCoCo 單元測試覆蓋率、build debug APK，含路由表、快速開始與檢查清單
- **imac-android-ci-pipeline/assets/** - `.gitlab-ci.yml`、Checkstyle / SpotBugs 規則檔、`.editorconfig`
- **imac-android-ci-pipeline/references/** - 版本查證、Android module 的 Gradle 設定、GitLab 設定、Android Studio 整合、疑難排解、適用限制與版本相容性
- **imac-android-ci-pipeline/scripts/git-hooks/** - 本地 pre-commit / pre-push hook 與安裝說明
- **git_skill/SKILL.md** - Android Team 在 GitLab 的協作流程：main/develop/feature 三層分支模型、分支命名與 commit message 強制格式、紅線規則（禁止對 main/develop 直接 push 或 pull）、rebase / MR / release 合併指令模板
- **git_skill/examples/** - Commit message 格式範例
- **git_skill/references/** - 分情境的詳細指南（onboarding、MR workflow、rebase、release 合併、非標準狀況處理）
- **git_skill/scripts/validate.sh** - 自動驗證分支名與 commit message 格式

## 如何在 Android Studio 中使用

### Claude Code 使用者

將此專案複製至 Claude Code 的 skills 目錄，三種方式：
1. download zip 然後解壓縮，放入 C:/User/username/.claude/skills/ 目錄底下
2. 使用 PowerShell 進入 C:/User/username/.claude/skills/ 使用 git clone 指令

    p.s. 目前各 AI 讀取 skills 不會遞迴掃描 skills 目錄下所有的 SKILL.md，所以使用上述兩個方式複製此專案後，請自行把專案內各 skill 目錄分開：
    ```
    skills/
    ├── imac-android-architecture-skill
    ├── imac-android-ci-pipeline
    └── git_skill
    ```
3. 找個資料夾 git clone 此專案，並把各 skill 目錄複製至自己的 C:/User/username/.claude/skills/ 目錄底下

#### 如何確認：

1. 終端輸入 claude
2. 在 claude code 輸入 /skills   ->   claude code 會顯示 project 層級的 skills & user 層級的 skills

### Gemini CLI 使用者

將此專案複製至 Gemini CLI 的 skills 目錄：
1. download zip 然後解壓縮，放入 C:/User/username/.gemini/skills/ 目錄底下
2. 使用 PowerShell 進入 C:/User/username/.gemini/skills/ 使用 git clone 指令

    p.s. 目前各 AI 讀取 skills 不會遞迴掃描 skills 目錄下所有的 SKILL.md，所以使用上述兩個方式複製此專案後，請自行把專案內各 skill 目錄分開：
    ```
    skills/
    ├── imac-android-architecture-skill
    ├── imac-android-ci-pipeline
    └── git_skill
    ```
3. 找個資料夾 git clone 此專案，並把各 skill 目錄複製至自己的 C:/User/username/.gemini/skills/ 目錄底下

#### 如何確認：
1. 終端輸入 gemini   or  gemini skills list
2. 在 gemini cli 輸入 /skills list   ->   gemini cli 會顯示 skills (如使用 gemini skills list 會顯示你的 skills 放在哪些資料夾)

### Github Copilot Plugin

先確認是否有Enable Skills
![image](https://github.com/noalowo/image/blob/main/%E8%9E%A2%E5%B9%95%E6%93%B7%E5%8F%96%E7%95%AB%E9%9D%A2%202026-04-01%20151246.png)

下面是Github Copilot Skills 官方文件可參考，內容(如下圖)提到 Copilot 會如何讀取專案 Skills 和個人 Skills：
https://docs.github.com/en/copilot/concepts/agents/about-agent-skills
![image](https://github.com/noalowo/image/blob/main/%E8%9E%A2%E5%B9%95%E6%93%B7%E5%8F%96%E7%95%AB%E9%9D%A2%202026-04-01%20152042.png)


## 安裝後，AI 會自動在處理 Android 相關任務時載入此技能，包含以下情境：

**架構開發（imac-android-architecture-skill）**
- 新增 Activity / 頁面 / 模組
- 建立 MVP 架構
- 新增 Retrofit API 端點
- 建立 RecyclerView Adapter
- 修改 AndroidManifest.xml
- 處理 Activity 生命週期（onResume/onPause/onSaveInstanceState 等）

**CI Pipeline（imac-android-ci-pipeline）**
- 設定 GitLab CI / 建立 `.gitlab-ci.yml`
- 建立自動化 pipeline（lint / sast / test / build）
- 程式碼品質檢查（Checkstyle / Android Lint）
- SAST 靜態安全檢測（SpotBugs + find-sec-bugs）
- 單元測試與覆蓋率（JUnit + JaCoCo）
- build debug APK
- 在 Android module 接上 Checkstyle / SpotBugs / JaCoCo 的 Gradle 設定
- 設定 pre-commit hook 與 Android Studio 規則檔同步

**Git 協作流程（git_skill）**
- 開新 feature 分支 / 建立 MR
- 撰寫或檢查 commit message 格式
- rebase develop / 同步 develop / 解衝突
- 合併到 develop / develop 合併到 main（release）
- 第一次 clone 專案、設定環境
- 在 Android 專案內 commit 時自動套用 commit message 規範

p.s. 此 skill 持續更新中..
