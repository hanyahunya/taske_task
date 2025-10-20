package com.hanyahunya.task.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateTaskActiveRequest(@NotNull Boolean isActive) {
}