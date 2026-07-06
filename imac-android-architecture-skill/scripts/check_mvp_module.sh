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

# ─── 檔案存在性 ────────────────────────────────────────────────────────

echo "--- 檔案結構 ---"
check_file "$MODULE_DIR/${MODULE_CLASS}Contract.java"
check_file "$MODULE_DIR/${MODULE_CLASS}Activity.java"
check_file "$MODULE_DIR/${MODULE_CLASS}Presenter.java"

echo ""

# ─── Contract 檢查 ─────────────────────────────────────────────────────

echo "--- Contract ---"
check_content "$MODULE_DIR/${MODULE_CLASS}Contract.java" "extends BaseContract.View"
check_content "$MODULE_DIR/${MODULE_CLASS}Contract.java" "extends BaseContract.Presenter"

echo ""

# ─── Presenter 檢查 ────────────────────────────────────────────────────

echo "--- Presenter ---"
check_content "$MODULE_DIR/${MODULE_CLASS}Presenter.java" "extends BasePresenter"
check_content "$MODULE_DIR/${MODULE_CLASS}Presenter.java" "super.onViewAttached"

# 確認 Presenter 不直接持有 Context（應透過 Repository）
if grep -q "private Context context" "$MODULE_DIR/${MODULE_CLASS}Presenter.java" 2>/dev/null; then
    echo "[WARN] ${MODULE_CLASS}Presenter.java 持有 Context — 建議改用 Repository 或 View 介面方法"
    FAIL=$((FAIL + 1))
else
    echo "[OK] ${MODULE_CLASS}Presenter.java 未直接持有 Context"
    PASS=$((PASS + 1))
fi

echo ""

# ─── Activity 檢查 ─────────────────────────────────────────────────────

echo "--- Activity ---"
check_content "$MODULE_DIR/${MODULE_CLASS}Activity.java" "extends BaseActivity"
check_content "$MODULE_DIR/${MODULE_CLASS}Activity.java" "inflateBinding"
check_content "$MODULE_DIR/${MODULE_CLASS}Activity.java" "createPresenter"
check_content "$MODULE_DIR/${MODULE_CLASS}Activity.java" "initViews"

# 確認 Activity 不使用 findViewById
if grep -q "findViewById" "$MODULE_DIR/${MODULE_CLASS}Activity.java" 2>/dev/null; then
    echo "[WARN] ${MODULE_CLASS}Activity.java 使用了 findViewById — 應使用 ViewBinding"
    FAIL=$((FAIL + 1))
else
    echo "[OK] ${MODULE_CLASS}Activity.java 未使用 findViewById（使用 ViewBinding）"
    PASS=$((PASS + 1))
fi

echo ""

# ─── Layout 檢查 ───────────────────────────────────────────────────────

echo "--- Layout ---"
# 從 Activity 中提取 Binding 類別名稱推斷 layout 檔名
MODULE_LOWER=$(echo "$MODULE_CLASS" | sed 's/\([A-Z]\)/_\L\1/g' | sed 's/^_//')
LAYOUT_FILE="app/src/main/res/layout/activity_${MODULE_LOWER}.xml"
check_file "$LAYOUT_FILE"

echo ""

# ─── AndroidManifest 檢查 ──────────────────────────────────────────────

echo "--- AndroidManifest ---"
MANIFEST="app/src/main/AndroidManifest.xml"
if [ -f "$MANIFEST" ]; then
    check_content "$MANIFEST" "${MODULE_CLASS}Activity"
else
    echo "[WARN] AndroidManifest.xml 未找到：$MANIFEST"
    FAIL=$((FAIL + 1))
fi

echo ""

# ─── 結果 ──────────────────────────────────────────────────────────────

echo "==============================="
echo "結果：$PASS 通過，$FAIL 需注意"
echo "==============================="

if [ $FAIL -gt 0 ]; then
    exit 1
fi
