package com.example.app.base;

/**
 * MVP Base Contract
 * 所有模組的 Contract 應繼承此介面，避免重複定義通用方法。
 *
 * 用法：
 *   interface SampleContract {
 *       interface View extends BaseContract.View { ... }
 *       interface Presenter extends BaseContract.Presenter<View> { ... }
 *   }
 */
public interface BaseContract {

    interface View {
        /** 顯示錯誤訊息（簡易版，適合不需區分錯誤類型的場景） */
        void showError(String error);

        /** 顯示分類錯誤訊息（搭配 ErrorType 使用） */
        void showError(ErrorType type, String message);

        /** 顯示/隱藏 Loading 狀態 */
        void showLoading(boolean isLoading);

        /** 關閉頁面 */
        void finishActivity();
    }

    interface Presenter<V extends View> {
        /** Activity onResume() 時呼叫，綁定 View 並啟動資源 */
        void onViewAttached(V view);

        /** Activity onPause() 時呼叫，暫停資源並解除 View 參考 */
        void onViewDetached();

        /** Activity onDestroy() 時呼叫，永久清理所有資源 */
        void onDestroy();
    }
}
