package com.hanyahunya.task.application.port.command;

import java.util.Map;

public record FireTriggerCommand(
        Long taskId,
        String capabilityId,
        Map<String, Object> triggerData
) {}