---
name: android-skill
description: This skill should be used when the user is working on an Android project and asks to "create a new Activity", "add a new page", "create a new module", "add a new feature", "create MVP structure", "add API endpoint", "add new layout", "add new screen", "add Retrofit API", "create RecyclerView adapter", "handle lifecycle", "onResume", "onPause", "onDestroy", "onSaveInstanceState", "activity lifecycle", "lifecycle methods", "save instance state", "restore state", or when the task involves creating or modifying Android Activities, Presenters, Contracts, layouts, adapters, API services, lifecycle callbacks, state management, or any Android component in a Java-based Android project.
version: 1.0.0
---

# Android 通用開發規範

此 skill 適用於所有 Android Java 專案。執行任務前，先讀取專案的 CLAUDE.md（若存在）以獲取專案特有規範，再結合以下通用規則。

## 命名規則

| 場景 | 格式 | 範例 |
|------|------|------|
| Java 類別 | PascalCase | `MainActivity`、`DialogueResponse` |
| Java 變數 / 方法 | camelCase | `conversationId`、`initViews()` |
| Java 常數 (static final) | UPPER_SNAKE_CASE | `TARGET_PACKAGE`、`REQUEST_CODE` |
| XML layout 檔名 | snake_case | `activity_main.xml`、`take_photo.xml` |
| XML drawable 檔名 | snake_case | `bg_button_purple.xml`、`ic_arrow.xml` |
| XML View ID | snake_case + 元件前綴 | `iv_back`、`btn_submit`、`tv_title` |
| Java 中 View 變數 | camelCase + 保留前綴 | `ivBack`、`btnSubmit`、`tvTitle` |
| JSON 欄位 (API) | snake_case | `conversation_id`、`user_message` |
| Java 對應 JSON 變數 | camelCase + @SerializedName | `conversationId` |
| Package 目錄 | 全小寫，可用底線 | `take_photo`、`shopping` |

### View ID 前綴對照

| 前綴 | 元件類型 |
|------|---------|
| `iv_` | ImageView |
| `tv_` | TextView |
| `btn_` | Button |
| `cv_` | CardView |
| `rv_` | RecyclerView |
| `ll_` | LinearLayout |
| `cl_` | ConstraintLayout |
| `sv_` | ScrollView |
| `et_` | EditText |
| `pv_` | PreviewView |
| `gl_` | GridLayout |

## MVP 架構範本

新增功能模組時，須建立三個檔案於 `ui/{module}/` 下：

### Contract（介面定義）

```java
package {package}.ui.{module};

public interface {Module}Contract {
    interface View {
        void showError(String error);
        void showLoading(boolean isLoading);
        void finishActivity();
    }
    interface Presenter {
        /** Activity onResume() 時呼叫 */
        void onViewAttached(View view);
        /** Activity onPause() 時呼叫 */
        void onViewDetached();
        /** Activity onDestroy() 時呼叫 */
        void onDestroy();
    }
}
```

### Activity（View 實作）

必須在 `onCreate()` 中執行的初始化：

```java
// 1. 隱藏 ActionBar
if (getSupportActionBar() != null) getSupportActionBar().hide();

// 2. 全螢幕 + 隱藏 System Navigation Bar（API 30 以上與以下寫法不同）
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    // API 30+：使用 WindowInsetsController
    WindowInsetsControllerCompat windowInsetsController =
            WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
    windowInsetsController.setSystemBarsBehavior(
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
} else {
    // API 29 以下：使用 setSystemUiVisibility（已棄用但低版本仍需要）
    getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
}

// 3. 螢幕常亮
getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
```

需要的 import：
```java
import android.os.Build;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
```

Activity 結構重點：
- `implements {Module}Contract.View`
- `onCreate()` 中呼叫 `initViews()` 綁定 UI 元件與事件；Presenter 建構子**不傳入 View**
- `onResume()` 中呼叫 `presenter.onViewAttached(this)`
- `onPause()` 中呼叫 `presenter.onViewDetached()`
- `onDestroy()` 中呼叫 `presenter.onDestroy()`
- UI 更新須用 `runOnUiThread()` 包裹

### Presenter（業務邏輯）

```java
package {package}.ui.{module};

import android.content.Context;
import java.lang.ref.WeakReference;

public class {Module}Presenter implements {Module}Contract.Presenter {

    private WeakReference<{Module}Contract.View> viewRef;
    private Context context;
    private CompositeDisposable disposables = new CompositeDisposable();

    public {Module}Presenter(Context context) {
        this.context = context;
        // 不在建構子持有 View，由 onViewAttached() 傳入
    }

    private {Module}Contract.View getView() {
        return viewRef != null ? viewRef.get() : null;
    }

    @Override
    public void onViewAttached({Module}Contract.View view) {
        viewRef = new WeakReference<>(view);
        // 啟動訂閱或資料載入
    }

    @Override
    public void onViewDetached() {
        if (disposables != null) disposables.clear();
        viewRef = null;
    }

    @Override
    public void onDestroy() {
        if (disposables != null) disposables.clear();
        viewRef = null;
        context = null;
    }
}
```

## Activity 生命週期規範

### 生命週期與 MVP 對應

| Activity 方法 | Presenter 對應 | 說明 |
|---|---|---|
| `onCreate()` | 建構 Presenter（不傳 View） | 初始化 UI、建立 Presenter 實例 |
| `onResume()` | `onViewAttached(this)` | 重新綁定 View、啟動訂閱/資源 |
| `onPause()` | `onViewDetached()` | 解除 View 參考、暫停訂閱/資源 |
| `onDestroy()` | `onDestroy()` | 清理 context、所有訂閱 |

**核心原則：Presenter 不直接依賴 Activity 生命週期**，只透過 `onViewAttached` / `onViewDetached` 感知 View 的存在。

### onResume / onPause 資源管理

在 Activity 層管理硬體資源（相機、感測器、計時器），不放進 Presenter：

```java
@Override
protected void onResume() {
    super.onResume();
    if (presenter != null) presenter.onViewAttached(this);
    // 重新啟動相機、感測器、計時器等
}

@Override
protected void onPause() {
    super.onPause();
    if (presenter != null) presenter.onViewDetached();
    // 暫停相機、感測器、計時器等，釋放資源
}
```

### onSaveInstanceState / onRestoreInstanceState

只存放**輕量 UI 狀態**（輸入框文字、捲動位置），不存放大型物件：

```java
@Override
protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString("input_text", etInput.getText().toString());
}

@Override
protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    etInput.setText(savedInstanceState.getString("input_text"));
}
```

> `onRestoreInstanceState` 在 `onStart()` 之後、`onResume()` 之前呼叫，bundle 保證非 null。

### Presenter 的 onViewAttached / onViewDetached

```java
@Override
public void onViewAttached(SampleContract.View view) {
    viewRef = new WeakReference<>(view);
    // 重新啟動需要 View 的訂閱或資料載入
}

@Override
public void onViewDetached() {
    if (disposables != null) disposables.clear();
    viewRef = null;
}
```

呼叫任何 view 方法前，務必先判斷 null：

```java
private SampleContract.View getView() {
    return viewRef != null ? viewRef.get() : null;
}

private void updateUi(String data) {
    SampleContract.View view = getView();
    if (view == null) return;   // View 已消失，直接略過
    view.showLoading(false);
}
```

### AndroidManifest.xml 註冊

新增 Activity 後務必在 Manifest 中註冊：

```xml
<activity android:name=".ui.{module}.{Module}Activity"
    android:exported="false"
    android:screenOrientation="landscape" />
```

注意：`screenOrientation` 依專案需求決定，先查看專案中其他 Activity 的設定保持一致。

## Retrofit API 規範

### 新增端點

- Request 類別放在 API 相關的 request 目錄
- Response 類別放在 API 相關的 response 目錄
- JSON 欄位一律使用 `@SerializedName("snake_case")` 映射至 camelCase 變數

```java
@SerializedName("conversation_id")
private String conversationId;
```

### HTTP 方法注意事項

- GET/DELETE 若需帶 request body，須使用 `@HTTP` 而非 `@GET`/`@DELETE`：

```java
@HTTP(method = "GET", path = "/api/resource", hasBody = true)
Call<Response> getResource(@Body Request request);

@HTTP(method = "DELETE", path = "/api/resource", hasBody = true)
Call<Response> deleteResource(@Body Request request);
```

## 大型資料 Intent 傳遞

當資料量可能超過 Intent 的 1MB 限制（TransactionTooLargeException）時：

1. 使用目標 Activity 的 **靜態變數** 暫存資料
2. Intent 中不放實際資料（或放 null）
3. 接收端 `onDestroy()` 中將靜態變數設為 null

```java
// 傳送端
TargetActivity.tempData = largeData;
intent.putExtra("DATA_KEY", (String) null);

// 接收端 onCreate()
if (tempData != null) { /* 使用 tempData */ }

// 接收端 onDestroy()
tempData = null;
```

## 檢查清單

建立或修改 Android 模組後，確認：
- [ ] Contract 定義了 View 和 Presenter 介面（含 `onViewAttached` / `onViewDetached` / `onDestroy`）
- [ ] Presenter 使用 WeakReference 持有 View，且在 `onViewDetached()` 將 viewRef 設為 null
- [ ] 呼叫 View 方法前均有 null 檢查（`getView() != null`）
- [ ] Activity 的 `onResume()` 呼叫 `presenter.onViewAttached(this)`
- [ ] Activity 的 `onPause()` 呼叫 `presenter.onViewDetached()`
- [ ] Activity 的 `onDestroy()` 呼叫 `presenter.onDestroy()`
- [ ] 需保留的 UI 狀態（輸入框等）透過 `onSaveInstanceState` / `onRestoreInstanceState` 處理
- [ ] 硬體資源（相機、感測器、計時器）在 Activity 的 `onResume` / `onPause` 管理，不放進 Presenter
- [ ] Activity 的 `onCreate()` 包含全螢幕、隱藏 Navigation Bar 等初始化（依專案慣例）
- [ ] AndroidManifest.xml 已註冊新 Activity
- [ ] Layout 中的 View ID 使用正確前綴
- [ ] 命名遵循上方命名規則表
- [ ] 新增的 API 欄位使用 @SerializedName
