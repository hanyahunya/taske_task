package com.hanyahunya.task.application.port.in;

import com.hanyahunya.task.application.port.command.FireTriggerCommand;

import java.util.Map;

/**
 * 트리거 발동후 사용할 위한 유스케이스
 */
public interface FireTriggerUseCase {
    void fireTrigger(FireTriggerCommand command);
}
