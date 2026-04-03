# Android Immersive Mode 參考

## API 30+ (WindowInsetsController)

官方文件：https://developer.android.com/develop/ui/views/layout/immersive

### 核心 API

| 類別 | 方法 | 說明 |
|------|------|------|
| `WindowCompat` | `getInsetsController(window, decorView)` | 取得 WindowInsetsControllerCompat |
| `WindowInsetsControllerCompat` | `hide(type)` | 隱藏指定系統列 |
| `WindowInsetsControllerCompat` | `show(type)` | 顯示指定系統列 |
| `WindowInsetsControllerCompat` | `setSystemBarsBehavior(behavior)` | 設定隱藏後的互動行為 |

### WindowInsetsCompat.Type

| Type | 說明 |
|------|------|
| `systemBars()` | Status Bar + Navigation Bar |
| `statusBars()` | 僅 Status Bar |
| `navigationBars()` | 僅 Navigation Bar |

### Behavior 常數

| 常數 | 說明 |
|------|------|
| `BEHAVIOR_SHOW_BARS_BY_TOUCH` | 任何觸控都會顯示系統列 |
| `BEHAVIOR_SHOW_BARS_BY_SWIPE` | 邊緣滑動才顯示系統列 |
| `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE` | 邊緣滑動暫時顯示半透明系統列（推薦用於沉浸式內容） |

### 必要 import

```java
import android.os.Build;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
```

## API 29 以下 (setSystemUiVisibility，已棄用)

### 常用 Flag 組合

| Flag | 說明 |
|------|------|
| `SYSTEM_UI_FLAG_FULLSCREEN` | 隱藏 Status Bar |
| `SYSTEM_UI_FLAG_HIDE_NAVIGATION` | 隱藏 Navigation Bar |
| `SYSTEM_UI_FLAG_IMMERSIVE` | 沉浸模式（首次觸控會恢復系統列） |
| `SYSTEM_UI_FLAG_IMMERSIVE_STICKY` | 黏性沉浸模式（滑動暫時顯示半透明系統列） |
| `SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN` | 讓內容延伸至 Status Bar 區域 |
| `SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION` | 讓內容延伸至 Navigation Bar 區域 |
| `SYSTEM_UI_FLAG_LAYOUT_STABLE` | 保持 layout 穩定，避免系統列顯示/隱藏造成 resize |
