package com.hanyahunya.task.application.port.response;

import com.hanyahunya.task.domain.model.Task;

import java.time.LocalDateTime;

public record TaskResponse(
        Long taskId,
        String taskName,
        boolean isActive,
        LocalDateTime updatedAt
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getTaskId(),
                task.getTaskName(),
                task.isActive(),
                task.getUpdatedAt()
        );
    }
}
