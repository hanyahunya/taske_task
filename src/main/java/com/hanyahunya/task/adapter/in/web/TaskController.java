package com.hanyahunya.task.adapter.in.web;

import com.hanyahunya.task.adapter.in.web.dto.CreateTaskRequest;
import com.hanyahunya.task.application.port.command.CreateTaskCommand;
import com.hanyahunya.task.application.port.in.TaskUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {
    private final TaskUseCase taskUseCase;

    @PostMapping
    public ResponseEntity<Void> createTask(@AuthenticationPrincipal String userId, @RequestBody @Valid CreateTaskRequest request) {
        CreateTaskCommand command = CreateTaskCommand.from(request, UUID.fromString(userId));
        taskUseCase.createTask(command);
        return ResponseEntity.ok().build();
    }
}
