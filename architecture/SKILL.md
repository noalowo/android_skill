---
name: android-skill
description: This skill should be used when the user is working on an Android project and asks to "create a new Activity", "add a new page", "create a new module", "add a new feature", "create MVP structure", "add API endpoint", "add new layout", "add new screen", "add Retrofit API", "create RecyclerView adapter", "handle lifecycle", "onResume", "onPause", "onDestroy", "onSaveInstanceState", "activity lifecycle", "lifecycle methods", "save instance state", "restore state", "ViewBinding", "view binding", "binding", "create Fragment", "add Fragment", "add Repository", "unit test", "test Presenter", or when the task involves creating or modifying Android Activities, Fragments, Presenters, Contracts, layouts, adapters, API services, repositories, lifecycle callbacks, state management, ViewBinding, or any Android component in a Java-based Android project.
version: 2.0.0
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
| `fl_` | FrameLayout |
| `sv_` | ScrollView |
| `et_` | EditText |
| `pv_` | PreviewView |
| `gl_` | GridLayout |
| `sw_` | Switch |
| `cb_` | CheckBox |
| `rb_` | RadioButton |
| `sb_` | SeekBar |
| `pb_` | ProgressBar |
| `sp_` | Spinner |
| `wv_` | WebView |
| `vp_` | ViewPager |
| `tl_` | TabLayout |
| `fab_` | FloatingActionButton |

## ViewBinding

### 啟用設定

在模組層級的 `build.gradle` 中啟用 ViewBinding：

```groovy
android {
    ...
    buildFeatures {
        viewBinding true
    }
}
```

啟用後，系統會為每個 XML layout 自動產生對應的 Binding 類別：
- `activity_main.xml` → `ActivityMainBinding`
- `fragment_home.xml` → `FragmentHomeBinding`
- `item_user.xml` → `ItemUserBinding`

命名規則：XML 檔名轉 PascalCase + `Binding` 後綴。

### View ID 與 Binding 欄位對應

XML 中的 snake_case ID 會自動轉為 camelCase 欄位：

| XML ID | Binding 欄位 |
|--------|-------------|
| `tv_title` | `binding.tvTitle` |
| `btn_submit` | `binding.btnSubmit` |
| `iv_back` | `binding.ivBack` |
| `rv_list` | `binding.rvList` |
| `et_input` | `binding.etInput` |

## MVP 架構

### 架構概覽

```
┌──────────────────────────────────────────────────┐
│                   View (Activity / Fragment)       │
│  - 顯示 UI、接收使用者操作                           │
│  - 實作 Contract.View 介面                          │
│  - 繼承 BaseActivity / BaseFragment                │
│  - 不包含業務邏輯                                    │
└────────────────────┬─────────────────────────────┘
                     │ Contract 介面
┌────────────────────┴─────────────────────────────┐
│                   Presenter                        │
│  - 處理業務邏輯                                      │
│  - 繼承 BasePresenter                               │
│  - 透過 Contract.View 更新 UI                        │
│  - 透過 Repository 取得資料                           │
│  - 不依賴 Android SDK（不持有 Context）               │
└────────────────────┬─────────────────────────────┘
                     │ Repository 介面
┌────────────────────┴─────────────────────────────┐
│                   Model (Repository)               │
│  - 管理資料來源（API / 本地快取 / SharedPreferences） │
│  - 提供統一的資料存取介面                              │
│  - 可獨立替換資料來源，不影響 Presenter                │
└──────────────────────────────────────────────────┘
```

### 目錄結構

```
com.example.app/
├── base/                          ← Base 類別
│   ├── BaseContract.java
│   ├── BasePresenter.java
│   ├── BaseActivity.java
│   ├── BaseFragment.java
│   └── ErrorType.java
├── data/                          ← Model 層
│   ├── remote/
│   │   └── ApiService.java        ← Retrofit 介面
│   ├── model/
│   │   ├── request/               ← API Request 類別
│   │   └── response/              ← API Response 類別
│   └── repository/
│       ├── SampleRepository.java      ← 介面
│       └── SampleRepositoryImpl.java  ← 實作
├── ui/                            ← View + Presenter 層
│   └── sample/
│       ├── SampleContract.java
│       ├── SampleActivity.java
│       ├── SamplePresenter.java
│       └── SampleAdapter.java
└── utils/
    └── rx/
        └── CallbackWrapper.java   ← RxJava 統一錯誤處理
```

### Base 類別

新增模組前，專案中應先建立以下 Base 類別（參考 `examples/` 下的範本）：

**BaseContract** — 定義所有模組共用的 View / Presenter 介面：
- `View`：`showError(String)`、`showError(ErrorType, String)`、`showLoading(boolean)`、`finishActivity()`
- `Presenter<V>`：`onViewAttached(V)`、`onViewDetached()`、`onDestroy()`

**BasePresenter\<V\>** — 封裝通用 Presenter 邏輯：
- `WeakReference<V>` 持有 View（含 `getView()`、`isViewAttached()` 方法）
- `CompositeDisposable` 管理 RxJava 訂閱
- `onViewDetached()` 呼叫 `disposables.clear()`（可重用）
- `onDestroy()` 呼叫 `disposables.dispose()`（永久銷毀）

**BaseActivity\<VB, P\>** — 封裝通用 Activity 邏輯：
- 子類實作三個抽象方法：`inflateBinding()`、`createPresenter()`、`initViews()`
- 自動處理：ViewBinding inflate、全螢幕沉浸模式、螢幕常亮、Presenter 生命週期綁定、`showError` 預設實作
- 可覆寫 `enableImmersiveMode()` / `enableKeepScreenOn()` 控制行為

**BaseFragment\<VB, P\>** — 封裝通用 Fragment 邏輯：
- 與 BaseActivity 類似，但 binding 在 `onDestroyView()` 設為 null（非 `onDestroy()`）

### Contract（介面定義）

模組 Contract 繼承 BaseContract，只定義模組特有的方法：

```java
public interface SampleContract {
    interface View extends BaseContract.View {
        void onDataLoaded(List<SampleItem> items);
    }
    interface Presenter extends BaseContract.Presenter<View> {
        void loadData();
    }
}
```

### Presenter（業務邏輯）

繼承 BasePresenter，透過 Repository 取得資料：

```java
public class SamplePresenter extends BasePresenter<SampleContract.View>
        implements SampleContract.Presenter {

    private final SampleRepository repository;

    // 不接收 Context → 可用純 JUnit 測試
    public SamplePresenter(SampleRepository repository) {
        this.repository = repository;
    }

    @Override
    public void onViewAttached(SampleContract.View view) {
        super.onViewAttached(view);  // 重要：必須呼叫 super
    }

    @Override
    public void loadData() {
        disposables.add(
            repository.fetchItems()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(d -> {
                    if (getView() != null) getView().showLoading(true);
                })
                .subscribe(
                    items -> {
                        if (getView() != null) {
                            getView().showLoading(false);
                            getView().onDataLoaded(items);
                        }
                    },
                    error -> {
                        if (getView() != null) {
                            getView().showLoading(false);
                            getView().showError(error.getMessage());
                        }
                    }
                )
        );
    }
}
```

**Presenter 設計原則：**
- **不持有 Context**：若需字串資源等，透過 `Contract.View` 介面方法取得
- **不依賴 Android SDK**：使業務邏輯可用純 JUnit + Mockito 測試
- **透過 Repository 取得資料**：不直接操作 Retrofit / Room
- **覆寫 `onViewAttached` 時必須呼叫 `super.onViewAttached(view)`**

### Activity（View 實作）

繼承 BaseActivity，只需實作三個抽象方法 + 模組特有的 View 方法：

```java
public class SampleActivity
        extends BaseActivity<ActivitySampleBinding, SamplePresenter>
        implements SampleContract.View {

    @Override
    protected ActivitySampleBinding inflateBinding(LayoutInflater inflater) {
        return ActivitySampleBinding.inflate(inflater);
    }

    @Override
    protected SamplePresenter createPresenter() {
        SampleRepositoryImpl repository = new SampleRepositoryImpl(ApiClient.getApiService());
        return new SamplePresenter(repository);
    }

    @Override
    protected void initViews() {
        // binding.btnSubmit.setOnClickListener(v -> { ... });
    }

    @Override
    public void onDataLoaded(List<SampleItem> items) {
        runOnUiThread(() -> { /* 更新 UI */ });
    }
}
```

### Fragment（View 實作）

繼承 BaseFragment，與 Activity 類似但有關鍵差異：

```java
public class SampleFragment
        extends BaseFragment<FragmentSampleBinding, SamplePresenter>
        implements SampleContract.View {

    @Override
    protected FragmentSampleBinding inflateBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentSampleBinding.inflate(inflater, container, false);
    }

    @Override
    protected SamplePresenter createPresenter() {
        SampleRepositoryImpl repository = new SampleRepositoryImpl(ApiClient.getApiService());
        return new SamplePresenter(repository);
    }

    @Override
    protected void initViews() {
        // binding.btnSubmit.setOnClickListener(v -> { ... });
    }

    @Override
    public void onDataLoaded(List<SampleItem> items) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> { /* 更新 UI */ });
        }
    }
}
```

**Fragment 與 Activity 的關鍵差異：**
- `inflateBinding` 需要額外的 `ViewGroup container` 參數
- binding 在 `onDestroyView()` 設為 null（非 `onDestroy()`），因為 Fragment 可能在 View 銷毀後仍存活（如 back stack）
- UI 更新需先檢查 `getActivity() != null`

### Model 層 / Repository Pattern

Repository 負責管理資料來源，提供統一介面給 Presenter：

```java
// 介面（Presenter 依賴此介面）
public interface SampleRepository {
    Observable<List<SampleItem>> fetchItems();
    Observable<SampleItem> fetchItemById(String id);
}

// 實作（依賴具體資料來源）
public class SampleRepositoryImpl implements SampleRepository {
    private final ApiService apiService;

    public SampleRepositoryImpl(ApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public Observable<List<SampleItem>> fetchItems() {
        return apiService.getItems()
                .map(response -> response.getData().getItemList());
    }
}
```

**Repository 設計原則：**
- Presenter 只依賴 Repository **介面**，不依賴具體實作
- Repository 可整合多種資料來源（API + 本地快取），Presenter 不需知道細節
- 方便單元測試時 Mock Repository

## Activity 生命週期規範

### 生命週期與 MVP 對應

| Activity 方法 | Presenter 對應 | 說明 |
|---|---|---|
| `onCreate()` | `createPresenter()`（不傳 View） | 初始化 UI、建立 Presenter 實例 |
| `onResume()` | `onViewAttached(this)` | 重新綁定 View、啟動訂閱/資源 |
| `onPause()` | `onViewDetached()` | 解除 View 參考、暫停訂閱/資源 |
| `onDestroy()` | `onDestroy()` | 清理所有訂閱與參考 |

> 使用 BaseActivity 時，上述生命週期綁定已自動處理，子類不需手動呼叫。

### clear() vs dispose() 的差異

| 方法 | 觸發時機 | 行為 | CompositeDisposable 狀態 |
|------|---------|------|------------------------|
| `clear()` | `onViewDetached()`（onPause） | 清除已加入的訂閱 | **仍可使用**，下次 `add()` 有效 |
| `dispose()` | `onDestroy()` | 清除並銷毀 | **不可再使用**，之後 `add()` 會立即 dispose |

### onResume / onPause 資源管理

硬體資源（相機、感測器、計時器）在 Activity 層管理，不放進 Presenter：

```java
@Override
protected void onResume() {
    super.onResume();  // BaseActivity 會呼叫 presenter.onViewAttached(this)
    // 重新啟動相機、感測器、計時器等
}

@Override
protected void onPause() {
    super.onPause();   // BaseActivity 會呼叫 presenter.onViewDetached()
    // 暫停相機、感測器、計時器等，釋放資源
}
```

### onSaveInstanceState / onRestoreInstanceState

只存放**輕量 UI 狀態**（輸入框文字、捲動位置），不存放大型物件：

```java
@Override
protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (binding != null) {
        outState.putString("input_text", binding.etInput.getText().toString());
    }
}

@Override
protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    if (binding != null) {
        binding.etInput.setText(savedInstanceState.getString("input_text"));
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

## RxJava 使用規範

### Presenter 中的標準訂閱寫法

```java
disposables.add(
    repository.fetchItems()
        .subscribeOn(Schedulers.io())              // IO 執行緒執行網路/資料庫
        .observeOn(AndroidSchedulers.mainThread())  // 主執行緒更新 UI
        .doOnSubscribe(d -> {
            if (getView() != null) getView().showLoading(true);
        })
        .subscribe(
            items -> {
                if (getView() != null) {
                    getView().showLoading(false);
                    getView().onDataLoaded(items);
                }
            },
            error -> {
                if (getView() != null) {
                    getView().showLoading(false);
                    getView().showError(error.getMessage());
                }
            }
        )
);
```

**重點：**
1. 所有訂閱必須加入 `disposables`（由 BasePresenter 管理生命週期）
2. `subscribeOn(Schedulers.io())` 指定資料操作的執行緒
3. `observeOn(AndroidSchedulers.mainThread())` 切回主執行緒更新 UI
4. 每次呼叫 View 方法前都要做 `getView() != null` 檢查
5. subscribe 的 `onError` 不可省略，否則會拋出 `OnErrorNotImplementedException` 導致閃退

### 使用 CallbackWrapper 的替代寫法

適合需要統一錯誤分類的場景（參考 `examples/CallbackWrapper.java`）：

```java
disposables.add(
    repository.fetchItems()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(new CallbackWrapper<List<SampleItem>>(getView()) {
            @Override
            public void onNext(List<SampleItem> items) {
                if (getView() != null) {
                    getView().showLoading(false);
                    getView().onDataLoaded(items);
                }
            }
        })
);
```

## 統一錯誤處理

### ErrorType 錯誤分類

| 類型 | 說明 | 對應 Exception |
|------|------|---------------|
| `NETWORK` | 無網路或網路不穩定 | `IOException`（排除 `SocketTimeoutException`） |
| `TIMEOUT` | 連線逾時 | `SocketTimeoutException` |
| `SERVER` | HTTP 4xx / 5xx | `HttpException` |
| `UNKNOWN` | 其他未預期錯誤 | 其他 `Throwable` |

**注意**：`SocketTimeoutException` 是 `IOException` 的子類，判斷時必須先檢查 `SocketTimeoutException`。

### BaseContract.View 錯誤方法

```java
// 簡易版 — 直接顯示錯誤訊息
void showError(String error);

// 分類版 — 根據錯誤類型顯示不同 UI
void showError(ErrorType type, String message);
```

BaseActivity 已提供兩者的預設實作（Toast），子類可覆寫以使用 Snackbar、Dialog 等。

## RecyclerView Adapter 規範

### 基本結構

```java
public class SampleAdapter extends RecyclerView.Adapter<SampleAdapter.SampleViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(SampleItem item, int position);
    }

    private final List<SampleItem> items = new ArrayList<>();
    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // ViewHolder 使用 ViewBinding
    class SampleViewHolder extends RecyclerView.ViewHolder {
        private final ItemSampleBinding binding;

        SampleViewHolder(ItemSampleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SampleItem item) {
            binding.tvTitle.setText(item.getTitle());
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onItemClick(items.get(pos), pos);
                    }
                }
            });
        }
    }
}
```

### 重點規範

- **ViewHolder 使用 ViewBinding**（`ItemSampleBinding`），不使用 `findViewById`
- **更新資料使用 DiffUtil**，避免 `notifyDataSetChanged()` 造成全部重繪（參考 `examples/SampleAdapterExample.java`）
- **點擊事件透過 `OnItemClickListener` 介面回傳**，由 Activity/Fragment 轉交 Presenter 處理
- **Adapter 不持有 Context**，透過 `parent.getContext()` 取得
- **取得位置使用 `getBindingAdapterPosition()`**，並檢查 `!= RecyclerView.NO_POSITION`

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

## 單元測試規範

### Presenter 測試原則

MVP 架構的最大優勢之一是 Presenter 的可測試性。因為 Presenter 不依賴 Android SDK，可用純 JUnit + Mockito 測試。

**測試目錄**：`app/src/test/java/`（非 androidTest）

**必要依賴**（build.gradle）：
```groovy
testImplementation 'junit:junit:4.13.2'
testImplementation 'org.mockito:mockito-core:5.11.0'
```

### 測試結構

```java
@RunWith(MockitoJUnitRunner.class)
public class SamplePresenterTest {

    @Rule
    public TrampolineSchedulerRule schedulerRule = new TrampolineSchedulerRule();

    @Mock SampleContract.View mockView;
    @Mock SampleRepository mockRepository;

    private SamplePresenter presenter;

    @Before
    public void setup() {
        presenter = new SamplePresenter(mockRepository);
        presenter.onViewAttached(mockView);
    }

    @Test
    public void loadData_success_showsData() {
        // Given
        when(mockRepository.fetchItems()).thenReturn(Observable.just(items));
        // When
        presenter.loadData();
        // Then
        verify(mockView).showLoading(false);
        verify(mockView).onDataLoaded(items);
    }

    @After
    public void tearDown() {
        presenter.onDestroy();
    }
}
```

**TrampolineSchedulerRule**：讓 RxJava 在測試中同步執行，避免非同步導致 `verify()` 失敗。完整實作參考 `examples/SamplePresenterTest.java`。

### 測試重點

- Mock `Contract.View` 和 `Repository`，驗證 Presenter 在各種情境下的行為
- 測試成功情境：verify 呼叫了正確的 View 方法
- 測試失敗情境：verify 呼叫了 `showError`
- 測試 View 已銷毀：verify 不應 crash 也不應有 View 互動

## 大型資料 Intent 傳遞

當資料量可能超過 Intent 的 1MB 限制（`TransactionTooLargeException`）時，依以下優先順序選擇方案：

### 方案一：只傳 ID，在目標 Activity 重新查詢（推薦）

```java
// 傳送端
intent.putExtra("ITEM_ID", itemId);

// 接收端 onCreate()
String id = getIntent().getStringExtra("ITEM_ID");
presenter.loadItemById(id);  // 透過 Repository 重新查詢
```

### 方案二：使用 Application-scoped 共享容器

```java
// 建立 Application 層級的暫存
public class AppDataHolder {
    private static Object tempData;
    public static void set(Object data) { tempData = data; }
    public static Object get() { return tempData; }
    public static void clear() { tempData = null; }
}

// 傳送端
AppDataHolder.set(largeData);
startActivity(intent);

// 接收端 onCreate()
Object data = AppDataHolder.get();
AppDataHolder.clear();
```

### 方案三：寫入本地暫存檔案

適合超大型資料（如圖片、長列表等），透過檔案路徑傳遞。

### 方案四：靜態變數（最後手段）

```java
// 傳送端
TargetActivity.tempData = largeData;
intent.putExtra("DATA_KEY", (String) null);

// 接收端 onDestroy()
tempData = null;
```

> **風險**：multi-process 或記憶體回收時可能丟失資料，僅在確認單 process 且資料為一次性使用時採用。

## 檢查清單

建立或修改 Android 模組後，確認：

### 架構
- [ ] 模組包含 Contract、Activity/Fragment、Presenter 三個檔案
- [ ] Contract 繼承 BaseContract（View extends BaseContract.View、Presenter extends BaseContract.Presenter\<View\>）
- [ ] Presenter 繼承 BasePresenter，覆寫 `onViewAttached` 時有呼叫 `super.onViewAttached(view)`
- [ ] Activity 繼承 BaseActivity 並實作 `inflateBinding`、`createPresenter`、`initViews`
- [ ] Fragment 繼承 BaseFragment（若使用 Fragment）
- [ ] Presenter 不持有 Context，透過 Repository 取得資料
- [ ] Repository 以介面定義，實作注入 Presenter

### ViewBinding
- [ ] 模組的 `build.gradle` 已啟用 `buildFeatures { viewBinding true }`
- [ ] Activity/Fragment 使用 ViewBinding，不使用 `findViewById`

### 生命週期（使用 BaseActivity/BaseFragment 時已自動處理，僅需確認自訂部分）
- [ ] 硬體資源（相機、感測器、計時器）在 onResume/onPause 管理
- [ ] 需保留的 UI 狀態透過 `onSaveInstanceState` / `onRestoreInstanceState` 處理

### RxJava
- [ ] 所有訂閱加入 `disposables`
- [ ] subscribe 包含 onError 回呼（避免 `OnErrorNotImplementedException`）
- [ ] 呼叫 View 方法前均有 `getView() != null` 檢查

### RecyclerView（若有）
- [ ] Adapter 的 ViewHolder 使用 ViewBinding
- [ ] 更新資料使用 DiffUtil（非 `notifyDataSetChanged`）
- [ ] 點擊事件透過介面回傳

### 其他
- [ ] AndroidManifest.xml 已註冊新 Activity
- [ ] Layout 中的 View ID 使用正確前綴
- [ ] 命名遵循上方命名規則表
- [ ] 新增的 API 欄位使用 `@SerializedName`
