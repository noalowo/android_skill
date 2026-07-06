package com.example.app.ui.sample;

import com.example.app.base.BasePresenter;
import com.example.app.data.repository.SampleRepository;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * MVP Presenter 範本（繼承 BasePresenter）
 * 用法：將 Sample 替換為實際模組名稱
 *
 * 改進重點（相對於直接實作）：
 *   1. 繼承 BasePresenter → WeakReference、CompositeDisposable、getView() 等通用邏輯已內建
 *   2. 不接收 Context → 可用純 JUnit + Mockito 測試，無需 Robolectric
 *   3. 透過 Repository 取得資料 → 解耦 API / 資料庫等資料來源
 *   4. clear() vs dispose() 已由 BasePresenter 處理
 *
 * 建構子參數：
 *   - Repository（必要）：資料來源
 *   - 若需要 Context 相關功能（如字串資源），透過 Contract.View 介面方法取得
 */
public class SamplePresenter extends BasePresenter<SampleContract.View>
        implements SampleContract.Presenter {

    private static final String TAG = "SamplePresenter";

    private final SampleRepository repository;

    public SamplePresenter(SampleRepository repository) {
        this.repository = repository;
    }

    // ─── 生命週期 Hook ────────────────────────────────────────────────────

    @Override
    public void onViewAttached(SampleContract.View view) {
        super.onViewAttached(view);  // 重要：必須呼叫 super 以設定 viewRef
        // 可在此自動載入資料，或由 View 層主動呼叫 loadData()
    }

    // ─── 業務邏輯 ─────────────────────────────────────────────────────────

    /**
     * RxJava 訂閱標準寫法：
     *   1. 加入 disposables（由 BasePresenter 管理，onViewDetached 時 clear、onDestroy 時 dispose）
     *   2. subscribeOn(Schedulers.io()) → 在 IO 執行緒執行網路/資料庫操作
     *   3. observeOn(AndroidSchedulers.mainThread()) → 在主執行緒更新 UI
     *   4. 更新 View 前務必用 getView() 做 null 檢查
     */
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

    // ─── 範例：使用 CallbackWrapper 的替代寫法 ─────────────────────────────
    //（適合需要統一錯誤處理的場景）

    /*
    public void loadDataWithCallbackWrapper() {
        disposables.add(
                repository.fetchItems()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(d -> {
                            if (getView() != null) getView().showLoading(true);
                        })
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
    }
    */
}
