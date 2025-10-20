package com.hanyahunya.task.application.port.command;

import com.hanyahunya.task.global.validation.IsAdminRole;
import io.micrometer.common.lang.NonNull;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record UpdateTaskActiveCommand(
        @NonNull Long taskId,
        UUID userId,
        String role,
        @NonNull Boolean isActive
) {
    public static UpdateTaskActiveCommand of(@NonNull Long taskId, @NonNull UUID userId, @NonNull Boolean isActive) {
        return new UpdateTaskActiveCommand(taskId, userId, null, isActive);
    }

    public static UpdateTaskActiveCommand ofAdmin(@NonNull Long taskId, @NotBlank @IsAdminRole String role, @NonNull Boolean isActive) {
        return new UpdateTaskActiveCommand(taskId, null, role, isActive);
    }
}
