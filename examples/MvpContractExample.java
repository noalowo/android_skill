package com.example.app.ui.sample;

/**
 * MVP Contract 範本
 * 用法：將 {Module} 替換為實際模組名稱
 *
 * 生命週期 hook 原則：
 *   - onResume()  → presenter.onViewAttached()  綁定 View、啟動資源
 *   - onPause()   → presenter.onViewDetached()  暫停資源、解除 View
 *   - onDestroy() → presenter.onDestroy()       清理所有訂閱與參考
 */
public interface SampleContract {

    interface View {
        void showError(String error);
        void showLoading(boolean isLoading);
        void finishActivity();
    }

    interface Presenter {
        /** Activity onResume() 時呼叫，重新綁定 View 並啟動資源 */
        void onViewAttached(View view);
        /** Activity onPause() 時呼叫，暫停資源並解除 View 強參考 */
        void onViewDetached();
        /** Activity onDestroy() 時呼叫，清理全部訂閱與 context */
        void onDestroy();
    }
}
