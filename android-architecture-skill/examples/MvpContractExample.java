package com.example.app.ui.sample;

import com.example.app.base.BaseContract;

import java.util.List;

/**
 * MVP Contract 範本（繼承 BaseContract）
 * 用法：將 Sample 替換為實際模組名稱
 *
 * BaseContract.View 已包含：showError、showLoading、finishActivity
 * BaseContract.Presenter 已包含：onViewAttached、onViewDetached、onDestroy
 * 此處只需定義模組特有的方法。
 *
 * 生命週期 hook 原則：
 *   - onResume()  → presenter.onViewAttached()  綁定 View、啟動資源
 *   - onPause()   → presenter.onViewDetached()  暫停資源、解除 View
 *   - onDestroy() → presenter.onDestroy()       清理所有訂閱與參考
 */
public interface SampleContract {

    interface View extends BaseContract.View {
        /** 資料載入完成時回呼 */
        void onDataLoaded(List<SampleItem> items);

        // 可依模組需求新增其他 View 方法，例如：
        // void onItemDeleted(int position);
        // void navigateToDetail(String id);
    }

    interface Presenter extends BaseContract.Presenter<View> {
        /** 載入資料 */
        void loadData();

        // 可依模組需求新增其他 Presenter 方法，例如：
        // void deleteItem(String id);
        // void refreshData();
    }
}
