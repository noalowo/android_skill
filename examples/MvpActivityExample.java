package com.example.app.ui.sample;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * MVP Activity 範本
 * 用法：將 Sample 替換為實際模組名稱，R.layout.sample 替換為實際 layout
 */
public class SampleActivity extends AppCompatActivity implements SampleContract.View {

    private static final String TAG = "SampleActivity";

    private SamplePresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 隱藏 ActionBar
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // 全螢幕 + 隱藏 System Navigation Bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsControllerCompat windowInsetsController =
                    WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            windowInsetsController.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        // 螢幕常亮
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // setContentView(R.layout.sample);

        // 建構子不傳 View，由 onResume() 的 onViewAttached() 負責
        presenter = new SamplePresenter(this);

        initViews();
    }

    private void initViews() {
        // 綁定 View 元件與事件
    }

    // ─── 生命週期 ─────────────────────────────────────────────────────────

    /**
     * onResume：Activity 回到前景，重新啟動資源與訂閱。
     * 常見場景：恢復相機、感測器、計時器、重新訂閱 RxJava 串流。
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (presenter != null) presenter.onViewAttached(this);
        // 範例：resumeCamera(); resumeSensor();
    }

    /**
     * onPause：Activity 離開前景，暫停消耗資源的操作。
     * 常見場景：暫停相機、感測器、停止計時器。
     * 注意：onSaveInstanceState 保證在 onStop 之前執行，不在此處呼叫 onViewDetached。
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (presenter != null) presenter.onViewDetached();
        // 範例：pauseCamera(); pauseSensor();
    }

    /**
     * onSaveInstanceState：系統即將回收 Activity 前儲存 UI 狀態。
     * 只存放輕量的 UI 狀態（輸入框內容、捲動位置等），不存放大型資料。
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // 範例：outState.putString("input_text", etInput.getText().toString());
    }

    /**
     * onRestoreInstanceState：系統重建 Activity 後還原狀態。
     * 在 onStart() 之後、onResume() 之前呼叫，bundle 保證非 null。
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // 範例：etInput.setText(savedInstanceState.getString("input_text"));
    }

    @Override
    protected void onDestroy() {
        if (presenter != null) presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void showError(String error) {
        runOnUiThread(() -> Toast.makeText(this, error, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void showLoading(boolean isLoading) {
        runOnUiThread(() -> {
            if (isLoading) Toast.makeText(this, "處理中...", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void finishActivity() {
        finish();
    }
}
