# Commit Message 範例集

格式：`[<type>] <簡述> - <developer>`。第一行必填；`Add:` / `Fix:` / `Refactor:` 條列 body 為選配，有其他變更才加。`<developer>` 強制小寫。規則詳見 `SKILL.md` §3.2。

---

## 標準範例

### feat
```
[feat] 新增登入 API 邏輯 - noah
```

完整版：
```
[feat] 新增登入 API 邏輯 - noah

Add:
- auth api endpoint integration
- 登入按鈕 click handler
- token 儲存到 SharedPreferences
```

### fix
```
[fix] 修正登入崩潰問題 - noah
```

完整版：
```
[fix] 修正首頁 RecyclerView 崩潰 - alice

Fix:
- adapter notifyDataSetChanged 在 onResume 時的 NPE
- 加上 null check 與 lifecycle 檢查
```

### refactor
```
[refactor] 登入邏輯部分簡化 - noah

Refactor:
- 把 LoginPresenter 的回呼改為 sealed class
- 移除重複的錯誤處理 helper
```

### docs
```
[docs] 補充 README 環境設定步驟 - bob
```

### chore
```
[chore] 升級 Retrofit 至 2.11.0 - noah
```

### test
```
[test] 新增 LoginPresenter 單元測試 - alice
```

### refactor + fix 混合
```
[refactor] 整理 API 錯誤處理 + 修正 timeout 沒被捕捉 - noah

Refactor:
- 抽出統一的 ApiErrorHandler
- 移除各 Presenter 重複的 try/catch

Fix:
- timeout 異常沒進入錯誤分支
```

---

## 不合法範例

| 訊息 | 問題 |
|---|---|
| `新增登入 API` | 缺類型 tag、缺開發者 |
| `[feat] 新增登入 API` | 缺 `- developer` |
| `feat: 新增登入 - noah` | 類型應用 `[]` 包起來 |
| `[Feat] 新增登入 - noah` | type 必須小寫 |
| `[feat]新增登入 - noah` | `]` 後缺空格 |
| `[feat] 新增登入- noah` | `-` 前後要空格 |
| `[feat] add login - Noah` | developer 必須小寫，`Noah` 不合法 |
