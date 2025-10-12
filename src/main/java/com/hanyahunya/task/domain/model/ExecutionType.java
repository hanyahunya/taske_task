package com.hanyahunya.task.domain.model;

public enum ExecutionType {
    SCHEDULING,
    HTTP_POLLING, // 주기적으로 API 호출 (Trigger 전용)
    WEBHOOK,      // 외부에서 요청 수신 (Trigger 전용)
    HTTP_REQUEST  // API 1회 호출 (Action 전용)
}
