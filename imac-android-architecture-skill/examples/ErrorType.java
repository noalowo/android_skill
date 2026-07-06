package com.example.app.base;

/**
 * 統一錯誤類型定義
 * 用於 MVP 架構中 View 層根據錯誤類型顯示不同的 UI 回饋
 */
public enum ErrorType {
    /** 無網路連線或網路不穩定 */
    NETWORK,
    /** 伺服器回傳錯誤（HTTP 4xx / 5xx） */
    SERVER,
    /** 連線逾時 */
    TIMEOUT,
    /** 未知錯誤 */
    UNKNOWN
}
