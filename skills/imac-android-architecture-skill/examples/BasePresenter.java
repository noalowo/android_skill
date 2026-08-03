package com.example.app.base;

import java.lang.ref.WeakReference;

import io.reactivex.disposables.CompositeDisposable;

/**
 * MVP Base Presenter
 * 封裝 WeakReference、CompositeDisposable 等通用邏輯，子類不需重複撰寫。
 *
 * 生命週期對應：
 *   Activity.onResume()  → onViewAttached(view)  綁定 View、可重新訂閱
 *   Activity.onPause()   → onViewDetached()       clear 訂閱、解除 View
 *   Activity.onDestroy() → onDestroy()            dispose 訂閱、永久清理
 *
 * clear() vs dispose() 差異：
 *   - clear()：清除已加入的訂閱，但 CompositeDisposable 仍可重複使用（下次 onViewAttached 可再 add）
 *   - dispose()：永久銷毀，之後 add() 的訂閱會立即被 dispose，不可再使用
 *
 * 用法：
 *   public class SamplePresenter extends BasePresenter<SampleContract.View>
 *           implements SampleContract.Presenter { ... }
 */
public abstract class BasePresenter<V extends BaseContract.View>
        implements BaseContract.Presenter<V> {

    private WeakReference<V> viewRef;
    protected CompositeDisposable disposables = new CompositeDisposable();

    /** 取得 View，呼叫前應先確認非 null */
    protected V getView() {
        return viewRef != null ? viewRef.get() : null;
    }

    /** 判斷 View 是否仍存在 */
    protected boolean isViewAttached() {
        return getView() != null;
    }

    @Override
    public void onViewAttached(V view) {
        viewRef = new WeakReference<>(view);
    }

    @Override
    public void onViewDetached() {
        // clear：清除訂閱但保持 CompositeDisposable 可重用
        disposables.clear();
        viewRef = null;
    }

    @Override
    public void onDestroy() {
        // dispose：永久銷毀，Activity 已結束不會再重新訂閱
        disposables.dispose();
        viewRef = null;
    }
}
