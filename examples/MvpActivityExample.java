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

        presenter = new SamplePresenter(this, this);

        initViews();
    }

    private void initViews() {
        // 綁定 View 元件與事件
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
