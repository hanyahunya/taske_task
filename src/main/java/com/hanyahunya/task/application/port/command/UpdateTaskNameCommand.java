package com.hanyahunya.task.application.port.command;

import io.micrometer.common.lang.NonNull;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record UpdateTaskNameCommand(
        @NonNull Long taskId,
        @NonNull UUID userId,
        @NotBlank String taskName
) {}