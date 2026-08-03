#!/usr/bin/env bash
# 驗證 skills/*/SKILL.md 的 frontmatter：開頭正確、name 與目錄一致、必要欄位齊全、
# 且沒有會讓 YAML 解析失敗的「未加引號冒號空格」。
# 用法：bash check_skills.sh
set -u

ROOT="$(cd "$(dirname "$0")" && pwd)"
FAIL=0

for md in "$ROOT"/skills/*/SKILL.md; do
    [ -f "$md" ] || continue
    name="$(basename "$(dirname "$md")")"

    content="$(tr -d '\r' < "$md")"

    if [ "$(printf '%s\n' "$content" | sed -n '1p')" != "---" ]; then
        echo "[FAIL] $name：第一行不是 ---（frontmatter 開頭錯誤）"; FAIL=1; continue
    fi

    fm="$(printf '%s\n' "$content" | awk 'NR==1{next} /^---$/{exit} {print}')"

    fm_name="$(printf '%s\n' "$fm" | sed -n 's/^name:[[:space:]]*//p' | head -1)"
    [ -n "$fm_name" ] || { echo "[FAIL] $name：缺少 name:"; FAIL=1; }
    printf '%s\n' "$fm" | grep -q '^description:' || { echo "[FAIL] $name：缺少 description:"; FAIL=1; }

    if [ -n "$fm_name" ] && [ "$fm_name" != "$name" ]; then
        echo "[FAIL] $name：name:（$fm_name）與目錄名不符"; FAIL=1
    fi

    while IFS= read -r line; do
        case "$line" in
            [a-z_]*:\ *)
                val="${line#*: }"
                case "$val" in
                    '"'*|"'"*) : ;;
                    *": "*) echo "[FAIL] $name：${line%%:*} 值裡有未加引號的冒號空格，YAML 會解析失敗"; FAIL=1 ;;
                esac ;;
        esac
    done <<< "$fm"
done

[ "$FAIL" -eq 0 ] && echo "OK：所有 skill frontmatter 檢查通過"
exit "$FAIL"
