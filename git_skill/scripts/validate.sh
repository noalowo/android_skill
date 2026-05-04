#!/usr/bin/env bash
# validate.sh - Validate branch name and commit message for Android Team Git workflow
#
# Usage:
#   ./validate.sh branch [branch-name]        # Validate branch name (default: current branch)
#   ./validate.sh commit "<commit-message>"   # Validate a commit message string
#   ./validate.sh commit-file <file-path>     # Validate commit message from file (for git commit-msg hook)
#   ./validate.sh                             # Auto: validate current branch + last commit message
#
# Install as commit-msg hook:
#   ln -s ../../skills/android_skill/git_skill/scripts/validate.sh .git/hooks/commit-msg
#   chmod +x .git/hooks/commit-msg
# When invoked by git as a commit-msg hook, $1 is the commit message file path.

set -e

BRANCH_REGEX='^feature/[a-z][a-z0-9]*_[a-zA-Z0-9._-]+$'
COMMIT_REGEX='^\[(feat|fix|refactor|docs|chore|test|style|perf|build|ci)\] .+ - [a-z][a-z0-9]*$'
PROTECTED_BRANCHES=("main" "develop")

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

err() { echo -e "${RED}✗ $*${NC}" >&2; }
ok()  { echo -e "${GREEN}✓ $*${NC}"; }
warn(){ echo -e "${YELLOW}⚠ $*${NC}"; }

validate_branch() {
    local branch="${1:-$(git branch --show-current 2>/dev/null)}"
    if [ -z "$branch" ]; then
        err "Cannot determine branch name"
        return 1
    fi

    for pb in "${PROTECTED_BRANCHES[@]}"; do
        if [ "$branch" = "$pb" ]; then
            err "You are on protected branch '$branch'. Do NOT commit here."
            err "Run: git checkout -b feature/<dev>_<area>"
            return 1
        fi
    done

    if [[ "$branch" =~ $BRANCH_REGEX ]]; then
        ok "Branch name '$branch' is valid"
        return 0
    else
        err "Branch name '$branch' does not match required pattern:"
        err "  feature/<developer>_<area-or-feature>"
        err "Examples: feature/noah_login_api, feature/alice_recyclerview-adapter"
        return 1
    fi
}

validate_commit_msg() {
    local msg="$1"
    local first_line
    first_line=$(printf '%s\n' "$msg" | head -n1)

    # Skip merge / revert auto-generated messages
    if [[ "$first_line" =~ ^Merge ]] || [[ "$first_line" =~ ^Revert ]]; then
        ok "Skipping merge/revert commit"
        return 0
    fi

    if [[ "$first_line" =~ $COMMIT_REGEX ]]; then
        ok "Commit message is valid: $first_line"
        return 0
    else
        err "Commit message does not match required format:"
        err "  [<type>] <summary> - <developer>"
        err "  type: feat|fix|refactor|docs|chore|test|style|perf|build|ci"
        err "First line was: $first_line"
        echo
        err "Examples:"
        err "  [feat] 新增登入 API - noah"
        err "  [fix] 修正首頁崩潰 - alice"
        return 1
    fi
}

mode="${1:-auto}"

case "$mode" in
    branch)
        validate_branch "$2"
        ;;
    commit)
        if [ -z "$2" ]; then
            err "Usage: $0 commit \"<message>\""
            exit 2
        fi
        validate_commit_msg "$2"
        ;;
    commit-file)
        if [ -z "$2" ] || [ ! -f "$2" ]; then
            err "Usage: $0 commit-file <path>"
            exit 2
        fi
        validate_commit_msg "$(cat "$2")"
        ;;
    auto)
        validate_branch || exit 1
        last_msg=$(git log -1 --pretty=%B 2>/dev/null || echo "")
        if [ -n "$last_msg" ]; then
            validate_commit_msg "$last_msg"
        fi
        ;;
    *)
        # If invoked as git commit-msg hook, $1 is a file path
        if [ -f "$mode" ]; then
            validate_commit_msg "$(cat "$mode")"
        else
            err "Unknown mode: $mode"
            err "Usage: $0 [branch|commit|commit-file|auto] [arg]"
            exit 2
        fi
        ;;
esac
