package com.hanyahunya.task.adapter.in.web;

import com.hanyahunya.task.adapter.in.web.dto.CreateTaskRequest;
import com.hanyahunya.task.adapter.in.web.dto.UpdateTaskActiveRequest;
import com.hanyahunya.task.adapter.in.web.dto.UpdateTaskNameRequest;
import com.hanyahunya.task.application.port.command.CreateTaskCommand;
import com.hanyahunya.task.application.port.command.DeleteTaskCommand;
import com.hanyahunya.task.application.port.command.UpdateTaskActiveCommand;
import com.hanyahunya.task.application.port.command.UpdateTaskNameCommand;
import com.hanyahunya.task.application.port.in.TaskUseCase;
import com.hanyahunya.task.application.port.response.TaskResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class TaskController {
    private final TaskUseCase taskUseCase;

    @PostMapping
    public ResponseEntity<Void> createTask(@AuthenticationPrincipal String userId, @RequestBody @Valid CreateTaskRequest request) {
        CreateTaskCommand command = CreateTaskCommand.from(request, UUID.fromString(userId));
        taskUseCase.createTask(command);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTask(@AuthenticationPrincipal String userId) {
        List<TaskResponse> tasks = taskUseCase.getTask(UUID.fromString(userId));
        return ResponseEntity.ok(tasks);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@AuthenticationPrincipal String userId, @PathVariable(name = "taskId") Long taskId) {
        DeleteTaskCommand command = DeleteTaskCommand.of(UUID.fromString(userId), taskId);
        taskUseCase.deleteTask(command);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{taskId}/active")
    public ResponseEntity<Void> updateTaskActive(
            @AuthenticationPrincipal String userId,
            @PathVariable(name = "taskId") Long taskId,
            @RequestBody @Valid UpdateTaskActiveRequest request) {

        UpdateTaskActiveCommand command = UpdateTaskActiveCommand.of(
                taskId,
                UUID.fromString(userId),
                request.isActive()
        );

        taskUseCase.updateTaskActive(command);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{taskId}/title")
    public ResponseEntity<Void> updateTaskTitle(
            @AuthenticationPrincipal String userId,
            @PathVariable(name = "taskId") Long taskId,
            @RequestBody @Valid UpdateTaskNameRequest request
    ) {
        UpdateTaskNameCommand command = new UpdateTaskNameCommand(taskId, UUID.fromString(userId), request.taskName());
        taskUseCase.updateTaskName(command);
        return ResponseEntity.ok().build();
    }
}
