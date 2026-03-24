#!/bin/bash
# 快速建立 MVP 模組的目錄與空白檔案
# 用法：bash create_mvp_module.sh <package_path> <module_name> <ModuleName>
# 範例：bash create_mvp_module.sh app/src/main/java/com/example/app ui/settings Settings

PACKAGE_PATH=$1
MODULE_NAME=$2
MODULE_CLASS=$3

if [ -z "$PACKAGE_PATH" ] || [ -z "$MODULE_NAME" ] || [ -z "$MODULE_CLASS" ]; then
    echo "用法：bash create_mvp_module.sh <package_path> <module_name> <ModuleName>"
    echo "範例：bash create_mvp_module.sh app/src/main/java/com/example/app ui/settings Settings"
    exit 1
fi

MODULE_DIR="$PACKAGE_PATH/$MODULE_NAME"

mkdir -p "$MODULE_DIR"
touch "$MODULE_DIR/${MODULE_CLASS}Contract.java"
touch "$MODULE_DIR/${MODULE_CLASS}Activity.java"
touch "$MODULE_DIR/${MODULE_CLASS}Presenter.java"

echo "已建立 MVP 模組："
echo "  $MODULE_DIR/${MODULE_CLASS}Contract.java"
echo "  $MODULE_DIR/${MODULE_CLASS}Activity.java"
echo "  $MODULE_DIR/${MODULE_CLASS}Presenter.java"
echo ""
echo "記得："
echo "  1. 在 AndroidManifest.xml 中註冊 ${MODULE_CLASS}Activity"
echo "  2. 建立對應的 layout XML"
