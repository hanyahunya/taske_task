package com.hanyahunya.task.application.service;

import com.hanyahunya.task.application.exception.EntityNotFoundException;
import com.hanyahunya.task.application.port.command.CreateTaskCommand;
import com.hanyahunya.task.application.port.command.DeleteTaskCommand;
import com.hanyahunya.task.application.port.command.UpdateTaskActiveCommand;
import com.hanyahunya.task.application.port.command.UpdateTaskNameCommand;
import com.hanyahunya.task.application.port.in.GetExecutionDetailsUseCase;
import com.hanyahunya.task.application.port.in.TaskUseCase;
import com.hanyahunya.task.application.port.response.TaskResponse;
import com.hanyahunya.task.application.validation.ConfigValidator;
import com.hanyahunya.task.application.validation.DependencyResolver;
import com.hanyahunya.task.domain.model.Action;
import com.hanyahunya.task.domain.model.ModuleCapability;
import com.hanyahunya.task.domain.model.Task;
import com.hanyahunya.task.domain.model.Trigger;
import com.hanyahunya.task.domain.repository.ActionRepository;
import com.hanyahunya.task.domain.repository.ModuleCapabilityRepository;
import com.hanyahunya.task.domain.repository.TaskRepository;
import com.hanyahunya.task.domain.repository.TriggerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService implements TaskUseCase, GetExecutionDetailsUseCase {

    private final TaskRepository taskRepository;
    private final TriggerRepository triggerRepository;
    private final ActionRepository actionRepository;
    private final ModuleCapabilityRepository moduleCapabilityRepository;

    private final ConfigValidator configValidator;
    private final DependencyResolver dependencyResolver;

    @Override
    @Transactional
    public void createTask(CreateTaskCommand command) {
        log.info("Task 생성 로직 시작. User ID: {}", command.userId());

        Task task = Task.builder()
                .taskName(command.taskName())
                .userId(command.userId())
                .isActive(true)
                .build();
        Task savedTask = taskRepository.save(task);
        log.info("Task 저장 완료. Task ID: {}, User ID: {}", savedTask.getTaskId(), savedTask.getUserId());

        // --- Trigger 생성 및 검증 ---
        CreateTaskCommand.TriggerCommand triggerCommand = command.trigger();
        ModuleCapability triggerCapability = moduleCapabilityRepository.findById(triggerCommand.capabilityId())
                .orElseThrow(() -> {
                    log.warn("잘못된 Trigger Capability ID 요청: {}", triggerCommand.capabilityId());
                    return new IllegalArgumentException("Invalid trigger capability id: " + triggerCommand.capabilityId());
                });

        log.debug("Task ID {}: Trigger config 검증 시작... (Capability ID: {})", savedTask.getTaskId(), triggerCapability.getCapabilityId());
        configValidator.validate(triggerCapability, triggerCommand.config());
        log.debug("Task ID {}: Trigger config 검증 완료.", savedTask.getTaskId());


        Trigger trigger = Trigger.builder()
                .task(savedTask)
                .capability(triggerCapability)
                .triggerConfig(triggerCommand.config())
                .build();
        triggerRepository.save(trigger);
        log.debug("Task ID {}: Trigger 저장 완료", savedTask.getTaskId());


        log.debug("Task ID {}: Action 의존성 해결 및 생성 시작...", savedTask.getTaskId());

        // DependencyResolver를 호출하여 의존성 해결, 순서 재정렬, 변수 재조정이 완료된 최종 Action 리스트
        List<Action> finalActions = dependencyResolver.resolveAndBuildActions(
                savedTask,
                command.actions()
        );

        actionRepository.saveAll(finalActions);
        log.info("Task ID {}: {}개의 Action(의존성 포함) 저장 완료", savedTask.getTaskId(), finalActions.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTask(UUID userId) {
        return taskRepository.findAllByUserIdOrderByTaskIdDesc(userId).stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteTask(DeleteTaskCommand command) {
        if (command.role() == null) {
            taskRepository.deleteByTaskIdAndUserId(command.taskId(), command.userId());
        } else {
            taskRepository.deleteById(command.taskId());
        }
    }

    @Override
    @Transactional
    public void updateTaskActive(UpdateTaskActiveCommand command) {
        Task task;
        if (command.role() == null) {
            task = taskRepository.findByTaskIdAndUserId(command.taskId(), command.userId())
                    .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + command.taskId()));
        } else {
            task = taskRepository.findById(command.taskId())
                    .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + command.taskId()));
        }
        task.updateActive(command.isActive());
    }

    @Override
    @Transactional
    public void updateTaskName(UpdateTaskNameCommand command) {
        Task task = taskRepository.findByTaskIdAndUserId(command.taskId(), command.userId())
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + command.taskId()));
        task.updateTaskName(command.taskName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Action> getExecutionDetails(Long taskId) {
        return actionRepository.findAllWithCapabilityAndModuleByTaskId(taskId);
    }
}