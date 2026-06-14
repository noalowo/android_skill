# GitLab 設定

## 分支保護

Settings → Repository → Protected branches：

- `main` / `develop`：Allowed to push = No one；Allowed to merge = Maintainers

## Merge Request 守門

Settings → Merge requests：

- 啟用 **Pipelines must succeed**：pipeline 失敗無法 merge
- 啟用 **All threads must be resolved**：review 留言需全部解決
- 不啟用 **Squash commits**：保持線性歷史與作者歸屬

## Free tier 的 SAST 行為

本 skill 的 SAST 由 `spotbugs` job（SpotBugs + find-sec-bugs）提供，純 Gradle 執行，不受 GitLab tier 限制，會在 pipeline 產出報告 artifact。

GitLab 另有內建 SAST template，但 Free tier 的差異要先讓團隊知道：

| 功能 | Free | Ultimate |
|---|---|---|
| SAST 掃描 job 執行、產出 JSON artifact | 會跑 | 會跑 |
| MR 上顯示「本次新增哪些漏洞」widget | 看不到 | 看得到 |
| Security Dashboard / 漏洞管理 / Advanced SAST | 無 | 有 |

結論：Free tier 用 find-sec-bugs 當 SAST 最實際，報告以 artifact 形式下載查看。若日後升級 Ultimate，再加 `Security/SAST.gitlab-ci.yml` template 取得 MR widget。

## Secret Detection（選用）

GitLab Secret Detection 在 Free tier 完整可用，可在 pipeline 加：

```yaml
include:
  - template: Security/Secret-Detection.gitlab-ci.yml
```

## CI/CD Variables

本 skill 只 build debug APK，debug 由 AGP 自動產生的 debug keystore 簽名，不需任何 keystore 變數。若未來要加 release 簽名（屬 CD 範圍），才需設定 protected + masked 的簽名變數。
