package com.hanyahunya.task.application.port.command;

import java.util.Map;
import java.util.UUID;

public record FireTriggerCommand(
        UUID userId,
        Long taskId,
        String capabilityId,
        Map<String, Object> triggerData
) {}