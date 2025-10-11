package com.hanyahunya.task.application.port.in;

import java.util.Map;

/**
 * 트리거 발동후 사용할 위한 유스케이스
 */
public interface FireTriggerUseCase {
    void fireTrigger(Long taskId, Map<String, Object> triggerData);
}
