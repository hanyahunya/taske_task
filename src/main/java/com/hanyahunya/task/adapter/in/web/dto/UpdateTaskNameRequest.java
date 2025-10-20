package com.hanyahunya.task.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateTaskNameRequest(@NotBlank String taskName) {
}
