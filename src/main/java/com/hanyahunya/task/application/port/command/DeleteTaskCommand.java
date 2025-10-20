package com.hanyahunya.task.application.port.command;

import com.hanyahunya.task.global.validation.IsAdminRole;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record DeleteTaskCommand(
        Long taskId,
        UUID userId,
        String role
) {

    public static DeleteTaskCommand of(@NotBlank UUID userId, @NotBlank Long taskId) {
        return new DeleteTaskCommand(taskId, userId, null);
    }

    public static DeleteTaskCommand ofAdmin(@NotBlank Long taskId, @NotBlank @IsAdminRole String role) {
        return new DeleteTaskCommand(taskId, null, role);
    }

}