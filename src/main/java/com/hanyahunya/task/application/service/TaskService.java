package com.hanyahunya.task.application.service;

import com.hanyahunya.task.application.exception.EntityNotFoundException;
import com.hanyahunya.task.application.port.command.CreateTaskCommand;
import com.hanyahunya.task.application.port.command.DeleteTaskCommand;
import com.hanyahunya.task.application.port.command.UpdateTaskActiveCommand;
import com.hanyahunya.task.application.port.command.UpdateTaskNameCommand;
import com.hanyahunya.task.application.port.in.TaskUseCase;
import com.hanyahunya.task.application.port.response.TaskResponse;
import com.hanyahunya.task.application.validation.ConfigValidator;
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
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService implements TaskUseCase {

    private final TaskRepository taskRepository;
    private final TriggerRepository triggerRepository;
    private final ActionRepository actionRepository;
    private final ModuleCapabilityRepository moduleCapabilityRepository;
    private final ConfigValidator configValidator;

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


        // --- Actions 생성 및 검증 ---
        List<CreateTaskCommand.ActionCommand> actionCommands = command.actions();
        List<Action> actions = IntStream.range(0, actionCommands.size())
                .mapToObj(i -> {
                    CreateTaskCommand.ActionCommand actionCommand = actionCommands.get(i);
                    ModuleCapability actionCapability = moduleCapabilityRepository.findById(actionCommand.capabilityId())
                            .orElseThrow(() -> {
                                log.warn("잘못된 Action Capability ID 요청: {}", actionCommand.capabilityId());
                                return new IllegalArgumentException("Invalid action capability id: " + actionCommand.capabilityId());
                            });

                    // Validator 호출 (Action)
                    log.debug("Task ID {}: Action {} config 검증 시작... (Capability ID: {})", savedTask.getTaskId(), i, actionCapability.getCapabilityId());
                    configValidator.validate(actionCapability, actionCommand.config());
                    log.debug("Task ID {}: Action {} config 검증 완료.", savedTask.getTaskId(), i);

                    return Action.builder()
                            .task(savedTask)
                            .capability(actionCapability)
                            .actionConfig(actionCommand.config())
                            .executionOrder(i) // 실행 순서
                            .build();
                })
                .collect(Collectors.toList());

        actionRepository.saveAll(actions);
        log.info("Task ID {}: {}개의 Action 저장 완료", savedTask.getTaskId(), actions.size());
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
}