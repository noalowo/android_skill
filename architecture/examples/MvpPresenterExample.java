package com.example.app.ui.sample;

import android.content.Context;

import java.lang.ref.WeakReference;

import io.reactivex.disposables.CompositeDisposable;

/**
 * MVP Presenter 範本
 * 用法：將 Sample 替換為實際模組名稱
 *
 * 生命週期對應：
 *   Activity.onResume()  → onViewAttached(view)   重新持有 View、啟動訂閱
 *   Activity.onPause()   → onViewDetached()        解除 View 參考、暫停訂閱
 *   Activity.onDestroy() → onDestroy()             清理 context 與所有資源
 */
public class SamplePresenter implements SampleContract.Presenter {

    private static final String TAG = "SamplePresenter";

    private WeakReference<SampleContract.View> viewRef;
    private Context context;
    private CompositeDisposable disposables = new CompositeDisposable();

    public SamplePresenter(Context context) {
        this.context = context;
        // 注意：不在建構子持有 View，改由 onViewAttached() 傳入
    }

    /** 取得 View，呼叫前應先確認非 null */
    private SampleContract.View getView() {
        return viewRef != null ? viewRef.get() : null;
    }

    // ─── 生命週期 Hook ────────────────────────────────────────────────────

    @Override
    public void onViewAttached(SampleContract.View view) {
        viewRef = new WeakReference<>(view);
        // 在此啟動需要 View 的訂閱或資料載入
    }

    @Override
    public void onViewDetached() {
        // 暫停/清除訂閱，避免在背景更新已消失的 UI
        if (disposables != null) {
            disposables.clear();
        }
        viewRef = null;
    }

    @Override
    public void onDestroy() {
        if (disposables != null) {
            disposables.clear();
        }
        viewRef = null;
        context = null;
    }

    // ─── 範例：安全更新 UI ────────────────────────────────────────────────

    private void exampleUpdateUi(String data) {
        SampleContract.View view = getView();
        if (view == null) return;  // View 已銷毀，直接略過
        view.showLoading(false);
        // view.onDataLoaded(data);
    }
}
