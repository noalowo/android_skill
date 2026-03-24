package com.example.app.ui.sample;

/**
 * MVP Contract 範本
 * 用法：將 {Module} 替換為實際模組名稱
 */
public interface SampleContract {

    interface View {
        void showError(String error);
        void showLoading(boolean isLoading);
        void finishActivity();
    }

    interface Presenter {
        void onDestroy();
    }
}
