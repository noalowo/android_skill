package com.example.app.ui.sample;

import android.os.Bundle;
import android.view.LayoutInflater;

import com.example.app.base.BaseActivity;
import com.example.app.data.remote.ApiClient;
import com.example.app.data.repository.SampleRepositoryImpl;
import com.example.app.databinding.ActivitySampleBinding;

import java.util.List;

/**
 * MVP Activity 範本（繼承 BaseActivity）
 * 用法：將 Sample 替換為實際模組名稱，ActivitySampleBinding 替換為實際 Binding 類別
 *
 * BaseActivity 已處理：
 *   - ViewBinding inflate + setContentView
 *   - 隱藏 ActionBar、全螢幕沉浸模式、螢幕常亮
 *   - onResume → presenter.onViewAttached(this)
 *   - onPause → presenter.onViewDetached()
 *   - onDestroy → presenter.onDestroy() + binding = null
 *   - showError、showLoading、finishActivity 的預設實作
 *
 * 子類只需實作：inflateBinding、createPresenter、initViews + 模組特有的 View 方法
 */
public class SampleActivity
        extends BaseActivity<ActivitySampleBinding, SamplePresenter>
        implements SampleContract.View {

    private static final String TAG = "SampleActivity";

    private SampleAdapter adapter;

    // ─── BaseActivity 抽象方法實作 ────────────────────────────────────────

    @Override
    protected ActivitySampleBinding inflateBinding(LayoutInflater inflater) {
        return ActivitySampleBinding.inflate(inflater);
    }

    @Override
    protected SamplePresenter createPresenter() {
        // 建立 Repository → 注入 Presenter
        // 若專案使用 Dagger/Hilt，可改為依賴注入
        SampleRepositoryImpl repository = new SampleRepositoryImpl(ApiClient.getApiService());
        return new SamplePresenter(repository);
    }

    @Override
    protected void initViews() {
        // 設定 RecyclerView
        adapter = new SampleAdapter();
        adapter.setOnItemClickListener((item, position) -> {
            // 處理點擊事件，可轉交 Presenter
            // presenter.onItemClicked(item);
        });
        binding.rvList.setAdapter(adapter);

        // 設定其他 View 事件
        // binding.btnRefresh.setOnClickListener(v -> presenter.loadData());
    }

    /**
     * 若不需要全螢幕模式，覆寫此方法回傳 false。
     * 預設為 true（由 BaseActivity 定義）。
     */
    // @Override
    // protected boolean enableImmersiveMode() { return false; }

    // ─── 生命週期（僅需處理模組特有的資源） ──────────────────────────────────
    // BaseActivity 已處理 presenter.onViewAttached / onViewDetached / onDestroy

    /**
     * onSaveInstanceState：系統即將回收 Activity 前儲存 UI 狀態。
     * 只存放輕量的 UI 狀態（輸入框內容、捲動位置等），不存放大型資料。
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // 範例：
        // if (binding != null) {
        //     outState.putString("input_text", binding.etInput.getText().toString());
        // }
    }

    /**
     * onRestoreInstanceState：系統重建 Activity 後還原狀態。
     * 在 onStart() 之後、onResume() 之前呼叫，bundle 保證非 null。
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // 範例：
        // if (binding != null) {
        //     binding.etInput.setText(savedInstanceState.getString("input_text"));
        // }
    }

    // ─── SampleContract.View 實作 ─────────────────────────────────────────

    @Override
    public void onDataLoaded(List<SampleItem> items) {
        runOnUiThread(() -> adapter.updateItems(items));
    }

    /**
     * 覆寫 showLoading 以顯示自訂的 Loading UI。
     * BaseActivity 的預設實作為空。
     */
    @Override
    public void showLoading(boolean isLoading) {
        runOnUiThread(() -> {
            // 範例：控制 ProgressBar 顯示/隱藏
            // binding.pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }
}
