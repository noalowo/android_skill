#!/bin/bash
# 檢查 MVP 模組是否完整
# 用法：bash check_mvp_module.sh <module_dir> <ModuleName>
# 範例：bash check_mvp_module.sh app/src/main/java/com/example/app/ui/settings Settings

MODULE_DIR=$1
MODULE_CLASS=$2

if [ -z "$MODULE_DIR" ] || [ -z "$MODULE_CLASS" ]; then
    echo "用法：bash check_mvp_module.sh <module_dir> <ModuleName>"
    exit 1
fi

PASS=0
FAIL=0

check_file() {
    if [ -f "$1" ]; then
        echo "[OK] $1"
        PASS=$((PASS + 1))
    else
        echo "[MISSING] $1"
        FAIL=$((FAIL + 1))
    fi
}

check_content() {
    if grep -q "$2" "$1" 2>/dev/null; then
        echo "[OK] $1 包含 $2"
        PASS=$((PASS + 1))
    else
        echo "[WARN] $1 缺少 $2"
        FAIL=$((FAIL + 1))
    fi
}

echo "=== 檢查 MVP 模組：${MODULE_CLASS} ==="
echo ""

# 檢查檔案存在
check_file "$MODULE_DIR/${MODULE_CLASS}Contract.java"
check_file "$MODULE_DIR/${MODULE_CLASS}Activity.java"
check_file "$MODULE_DIR/${MODULE_CLASS}Presenter.java"

echo ""

# 檢查 Presenter 是否有 WeakReference
check_content "$MODULE_DIR/${MODULE_CLASS}Presenter.java" "WeakReference"

# 檢查 Activity 是否呼叫 presenter.onDestroy()
check_content "$MODULE_DIR/${MODULE_CLASS}Activity.java" "presenter.onDestroy()"

# 檢查 Activity 是否隱藏 ActionBar
check_content "$MODULE_DIR/${MODULE_CLASS}Activity.java" "getSupportActionBar"

# 檢查 AndroidManifest
MANIFEST="app/src/main/AndroidManifest.xml"
if [ -f "$MANIFEST" ]; then
    check_content "$MANIFEST" "${MODULE_CLASS}Activity"
fi

echo ""
echo "結果：$PASS 通過，$FAIL 需注意"
