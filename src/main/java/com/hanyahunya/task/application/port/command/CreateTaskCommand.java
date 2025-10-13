package com.hanyahunya.task.application.port.command;

import com.hanyahunya.task.adapter.in.web.dto.CreateTaskRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public record CreateTaskCommand(
        UUID userId,
        String taskName,
        TriggerCommand trigger,
        List<ActionCommand> actions
) {
    public record TriggerCommand(
            String capabilityId,
            Map<String, Object> config
    ) {}

    public record ActionCommand(
            String capabilityId,
            Map<String, Object> config
    ) {}

    public static CreateTaskCommand from(CreateTaskRequest request, UUID userId) {
        TriggerCommand triggerCommand = new TriggerCommand(
                request.trigger().capabilityId(),
                request.trigger().config()
        );

        List<ActionCommand> actionCommand = request.actions().stream()
                .map(action -> new ActionCommand(
                        action.capabilityId(),
                        action.config()
                ))
                .collect(Collectors.toList());

        return new CreateTaskCommand(
                userId,
                request.taskName(),
                triggerCommand,
                actionCommand
        );
    }
}
