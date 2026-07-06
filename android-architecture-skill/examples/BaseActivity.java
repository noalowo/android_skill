package com.example.app.base;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.viewbinding.ViewBinding;

/**
 * MVP Base Activity
 * 封裝 ViewBinding inflate、全螢幕初始化、Presenter 生命週期綁定等通用邏輯。
 *
 * 子類只需實作三個抽象方法：
 *   - inflateBinding()  → 回傳 ViewBinding 實例
 *   - createPresenter()  → 回傳 Presenter 實例
 *   - initViews()        → 初始化 View 元件與事件綁定
 *
 * 用法：
 *   public class SampleActivity
 *           extends BaseActivity<ActivitySampleBinding, SamplePresenter>
 *           implements SampleContract.View { ... }
 *
 * @param <VB> ViewBinding 類型
 * @param <P>  Presenter 類型
 */
public abstract class BaseActivity<VB extends ViewBinding, P extends BaseContract.Presenter>
        extends AppCompatActivity implements BaseContract.View {

    protected VB binding;
    protected P presenter;

    // ─── 子類必須實作 ─────────────────────────────────────────────────────

    /** inflate ViewBinding，例如 ActivitySampleBinding.inflate(inflater) */
    protected abstract VB inflateBinding(LayoutInflater inflater);

    /** 建立 Presenter 實例，例如 new SamplePresenter(repository) */
    protected abstract P createPresenter();

    /** 初始化 View 元件與事件綁定，此時 binding 和 presenter 皆已就緒 */
    protected abstract void initViews();

    // ─── 可覆寫的設定 ──────────────────────────────────────────────────────

    /** 是否啟用全螢幕沉浸模式，預設 true，子類可覆寫為 false */
    protected boolean enableImmersiveMode() {
        return true;
    }

    /** 是否保持螢幕常亮，預設 true，子類可覆寫為 false */
    protected boolean enableKeepScreenOn() {
        return true;
    }

    // ─── 生命週期 ─────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 隱藏 ActionBar
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // 全螢幕沉浸模式
        if (enableImmersiveMode()) {
            setupImmersiveMode();
        }

        // 螢幕常亮
        if (enableKeepScreenOn()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        // ViewBinding
        binding = inflateBinding(getLayoutInflater());
        setContentView(binding.getRoot());

        // Presenter
        presenter = createPresenter();

        // 初始化 View
        initViews();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onResume() {
        super.onResume();
        if (presenter != null) presenter.onViewAttached(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onPause() {
        super.onPause();
        if (presenter != null) presenter.onViewDetached();
    }

    @Override
    protected void onDestroy() {
        if (presenter != null) presenter.onDestroy();
        binding = null;
        super.onDestroy();
    }

    // ─── BaseContract.View 預設實作 ────────────────────────────────────────

    @Override
    public void showError(String error) {
        runOnUiThread(() -> Toast.makeText(this, error, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void showError(ErrorType type, String message) {
        runOnUiThread(() -> {
            switch (type) {
                case NETWORK:
                    Toast.makeText(this, "網路連線異常：" + message, Toast.LENGTH_LONG).show();
                    break;
                case TIMEOUT:
                    Toast.makeText(this, "連線逾時，請稍後再試", Toast.LENGTH_LONG).show();
                    break;
                case SERVER:
                    Toast.makeText(this, "伺服器錯誤：" + message, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }

    @Override
    public void showLoading(boolean isLoading) {
        // 子類覆寫以顯示自訂的 Loading UI（ProgressBar、Dialog 等）
    }

    @Override
    public void finishActivity() {
        finish();
    }

    // ─── 內部方法 ──────────────────────────────────────────────────────────

    private void setupImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsControllerCompat controller =
                    WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            controller.hide(WindowInsetsCompat.Type.systemBars());
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}
