package com.example.app.utils.rx;

import com.example.app.base.BaseContract;
import com.example.app.base.ErrorType;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;

import io.reactivex.observers.DisposableObserver;
import retrofit2.HttpException;

/**
 * RxJava 統一錯誤處理包裝器
 * 繼承 DisposableObserver，在 onError 中自動分類錯誤並通知 View。
 *
 * 使用 WeakReference 持有 View，避免記憶體洩漏。
 *
 * 錯誤分類邏輯：
 *   SocketTimeoutException → ErrorType.TIMEOUT  （須在 IOException 之前檢查，因為它是 IOException 的子類）
 *   IOException            → ErrorType.NETWORK
 *   HttpException          → ErrorType.SERVER
 *   其他                    → ErrorType.UNKNOWN
 *
 * 用法：
 *   disposables.add(
 *       repository.fetchItems()
 *           .subscribeOn(Schedulers.io())
 *           .observeOn(AndroidSchedulers.mainThread())
 *           .subscribeWith(new CallbackWrapper<List<Item>>(getView()) {
 *               @Override
 *               public void onNext(List<Item> items) {
 *                   if (getView() != null) getView().onDataLoaded(items);
 *               }
 *           })
 *   );
 */
public abstract class CallbackWrapper<T> extends DisposableObserver<T> {

    private final WeakReference<BaseContract.View> viewRef;

    public CallbackWrapper(BaseContract.View view) {
        this.viewRef = new WeakReference<>(view);
    }

    protected BaseContract.View getView() {
        return viewRef != null ? viewRef.get() : null;
    }

    @Override
    public void onComplete() {
        // 預設不處理，子類可覆寫
    }

    @Override
    public void onError(Throwable e) {
        BaseContract.View view = getView();
        if (view == null) return;

        view.showLoading(false);

        // 注意：SocketTimeoutException 是 IOException 的子類，必須先檢查
        if (e instanceof SocketTimeoutException) {
            view.showError(ErrorType.TIMEOUT, "連線逾時，請稍後再試");
        } else if (e instanceof IOException) {
            view.showError(ErrorType.NETWORK, "網路連線異常，請檢查網路設定");
        } else if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;
            int code = httpException.code();
            String message = httpException.message();
            view.showError(ErrorType.SERVER, "伺服器錯誤 (" + code + "): " + message);
        } else {
            view.showError(ErrorType.UNKNOWN,
                    e.getMessage() != null ? e.getMessage() : "發生未知錯誤");
        }
    }
}
