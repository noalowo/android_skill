#!/bin/bash
# 快速建立 MVP 模組的目錄與範本檔案
# 用法：bash create_mvp_module.sh <package_path> <module_name> <ModuleName> <full_package>
# 範例：bash create_mvp_module.sh app/src/main/java/com/example/app ui/settings Settings com.example.app

PACKAGE_PATH=$1
MODULE_NAME=$2
MODULE_CLASS=$3
FULL_PACKAGE=$4

if [ -z "$PACKAGE_PATH" ] || [ -z "$MODULE_NAME" ] || [ -z "$MODULE_CLASS" ] || [ -z "$FULL_PACKAGE" ]; then
    echo "用法：bash create_mvp_module.sh <package_path> <module_name> <ModuleName> <full_package>"
    echo "範例：bash create_mvp_module.sh app/src/main/java/com/example/app ui/settings Settings com.example.app"
    exit 1
fi

MODULE_DIR="$PACKAGE_PATH/$MODULE_NAME"
# 將 ui/settings 轉為 ui.settings
MODULE_PKG=$(echo "$MODULE_NAME" | tr '/' '.')
PACKAGE="${FULL_PACKAGE}.${MODULE_PKG}"

# 轉小寫作為 layout 檔名前綴
MODULE_LOWER=$(echo "$MODULE_CLASS" | sed 's/\([A-Z]\)/_\L\1/g' | sed 's/^_//')

mkdir -p "$MODULE_DIR"

# ─── Contract ─────────────────────────────────────────────────────────

cat > "$MODULE_DIR/${MODULE_CLASS}Contract.java" << JAVA
package ${PACKAGE};

import ${FULL_PACKAGE}.base.BaseContract;

import java.util.List;

public interface ${MODULE_CLASS}Contract {

    interface View extends BaseContract.View {
        // TODO: 定義模組特有的 View 方法
    }

    interface Presenter extends BaseContract.Presenter<View> {
        // TODO: 定義模組特有的 Presenter 方法
    }
}
JAVA

# ─── Presenter ─────────────────────────────────────────────────────────

cat > "$MODULE_DIR/${MODULE_CLASS}Presenter.java" << JAVA
package ${PACKAGE};

import ${FULL_PACKAGE}.base.BasePresenter;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ${MODULE_CLASS}Presenter extends BasePresenter<${MODULE_CLASS}Contract.View>
        implements ${MODULE_CLASS}Contract.Presenter {

    private static final String TAG = "${MODULE_CLASS}Presenter";

    // TODO: 注入 Repository
    // private final ${MODULE_CLASS}Repository repository;

    public ${MODULE_CLASS}Presenter(/* ${MODULE_CLASS}Repository repository */) {
        // this.repository = repository;
    }

    @Override
    public void onViewAttached(${MODULE_CLASS}Contract.View view) {
        super.onViewAttached(view);
        // TODO: 啟動需要 View 的訂閱或資料載入
    }
}
JAVA

# ─── Activity ──────────────────────────────────────────────────────────

BINDING_CLASS="Activity${MODULE_CLASS}Binding"

cat > "$MODULE_DIR/${MODULE_CLASS}Activity.java" << JAVA
package ${PACKAGE};

import android.os.Bundle;
import android.view.LayoutInflater;

import ${FULL_PACKAGE}.base.BaseActivity;
import ${FULL_PACKAGE}.databinding.${BINDING_CLASS};

public class ${MODULE_CLASS}Activity
        extends BaseActivity<${BINDING_CLASS}, ${MODULE_CLASS}Presenter>
        implements ${MODULE_CLASS}Contract.View {

    private static final String TAG = "${MODULE_CLASS}Activity";

    @Override
    protected ${BINDING_CLASS} inflateBinding(LayoutInflater inflater) {
        return ${BINDING_CLASS}.inflate(inflater);
    }

    @Override
    protected ${MODULE_CLASS}Presenter createPresenter() {
        // TODO: 建立 Repository 並注入 Presenter
        return new ${MODULE_CLASS}Presenter();
    }

    @Override
    protected void initViews() {
        // TODO: 初始化 View 元件與事件綁定
    }
}
JAVA

# ─── Layout XML ────────────────────────────────────────────────────────

LAYOUT_DIR="app/src/main/res/layout"
LAYOUT_FILE="activity_${MODULE_LOWER}.xml"

if [ -d "$LAYOUT_DIR" ]; then
    cat > "$LAYOUT_DIR/$LAYOUT_FILE" << XML
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- TODO: 新增 View 元件 -->

</androidx.constraintlayout.widget.ConstraintLayout>
XML
    echo "  $LAYOUT_DIR/$LAYOUT_FILE"
else
    echo "  [WARN] layout 目錄不存在：$LAYOUT_DIR，請手動建立 $LAYOUT_FILE"
fi

echo ""
echo "已建立 MVP 模組："
echo "  $MODULE_DIR/${MODULE_CLASS}Contract.java"
echo "  $MODULE_DIR/${MODULE_CLASS}Presenter.java"
echo "  $MODULE_DIR/${MODULE_CLASS}Activity.java"
echo ""
echo "記得："
echo "  1. 在 AndroidManifest.xml 中註冊 ${MODULE_CLASS}Activity"
echo "  2. 建立對應的 Repository（若有 API 需求）"
echo "  3. 確認 layout XML 中的 View ID 使用正確前綴"
