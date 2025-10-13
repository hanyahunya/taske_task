package com.hanyahunya.task.application.service;

import com.hanyahunya.task.application.port.command.CreateTaskCommand;
import com.hanyahunya.task.application.port.in.TaskUseCase;
import com.hanyahunya.task.domain.model.Action;
import com.hanyahunya.task.domain.model.ModuleCapability;
import com.hanyahunya.task.domain.model.Task;
import com.hanyahunya.task.domain.model.Trigger;
import com.hanyahunya.task.domain.repository.ActionRepository;
import com.hanyahunya.task.domain.repository.ModuleCapabilityRepository;
import com.hanyahunya.task.domain.repository.TaskRepository;
import com.hanyahunya.task.domain.repository.TriggerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class TaskService implements TaskUseCase {

    private final TaskRepository taskRepository;
    private final TriggerRepository triggerRepository;
    private final ActionRepository actionRepository;
    private final ModuleCapabilityRepository moduleCapabilityRepository;

    @Override
    @Transactional
    public void createTask(CreateTaskCommand command) {
        // todo ModelCa[ability의 설계도와 현재 들어온 config 정보가 일치하는지

        Task task = Task.builder()
                .taskName(command.taskName())
                .userId(command.userId())
                .isActive(true)
                .build();
        Task savedTask = taskRepository.save(task);

        // Trigger 생성 및 저장
        CreateTaskCommand.TriggerCommand triggerCommand = command.trigger();
        ModuleCapability triggerCapability = moduleCapabilityRepository.findById(triggerCommand.capabilityId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid trigger capability id: " + triggerCommand.capabilityId()));

        Trigger trigger = Trigger.builder()
                .task(savedTask)
                .capability(triggerCapability)
                .triggerConfig(triggerCommand.config())
                .build();
        triggerRepository.save(trigger);

        // Actions 생성 및 저장
        List<CreateTaskCommand.ActionCommand> actionCommands = command.actions();
        List<Action> actions = IntStream.range(0, actionCommands.size())
                .mapToObj(i -> {
                    CreateTaskCommand.ActionCommand actionCommand = actionCommands.get(i);
                    ModuleCapability actionCapability = moduleCapabilityRepository.findById(actionCommand.capabilityId())
                            .orElseThrow(() -> new IllegalArgumentException("Invalid action capability id: " + actionCommand.capabilityId()));

                    return Action.builder()
                            .task(savedTask)
                            .capability(actionCapability)
                            .actionConfig(actionCommand.config())
                            .executionOrder(i + 1) // 실행 순서
                            .build();
                })
                .collect(Collectors.toList());
        actionRepository.saveAll(actions);
    }
}