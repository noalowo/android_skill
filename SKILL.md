---
name: android-skill
description: This skill should be used when the user is working on an Android project and asks to "create a new Activity", "add a new page", "create a new module", "add a new feature", "create MVP structure", "add API endpoint", "add new layout", "add new screen", "add Retrofit API", "create RecyclerView adapter", or when the task involves creating or modifying Android Activities, Presenters, Contracts, layouts, adapters, API services, or any Android component in a Java-based Android project.
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
- `onCreate()` 中呼叫 `initViews()` 綁定 UI 元件與事件
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

    public {Module}Presenter({Module}Contract.View view, Context context) {
        this.viewRef = new WeakReference<>(view);
        this.context = context;
    }

    private {Module}Contract.View getView() {
        return viewRef != null ? viewRef.get() : null;
    }

    @Override
    public void onDestroy() {
        viewRef = null;
        context = null;
    }
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
- [ ] Contract 定義了 View 和 Presenter 介面
- [ ] Presenter 使用 WeakReference 持有 View
- [ ] Activity 的 onDestroy() 呼叫 presenter.onDestroy()
- [ ] Activity 的 onCreate() 包含全螢幕、隱藏 Navigation Bar 等初始化（依專案慣例）
- [ ] AndroidManifest.xml 已註冊新 Activity
- [ ] Layout 中的 View ID 使用正確前綴
- [ ] 命名遵循上方命名規則表
- [ ] 新增的 API 欄位使用 @SerializedName
