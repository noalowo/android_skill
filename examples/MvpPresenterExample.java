package com.example.app.ui.sample;

import android.content.Context;

import java.lang.ref.WeakReference;

import io.reactivex.disposables.CompositeDisposable;

/**
 * MVP Presenter 範本
 * 用法：將 Sample 替換為實際模組名稱
 */
public class SamplePresenter implements SampleContract.Presenter {

    private static final String TAG = "SamplePresenter";

    private WeakReference<SampleContract.View> viewRef;
    private Context context;
    private CompositeDisposable disposables = new CompositeDisposable();

    public SamplePresenter(SampleContract.View view, Context context) {
        this.viewRef = new WeakReference<>(view);
        this.context = context;
    }

    private SampleContract.View getView() {
        return viewRef != null ? viewRef.get() : null;
    }

    @Override
    public void onDestroy() {
        if (disposables != null) {
            disposables.clear();
        }
        viewRef = null;
        context = null;
    }
}
