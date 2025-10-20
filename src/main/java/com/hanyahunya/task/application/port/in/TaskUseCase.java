package com.hanyahunya.task.application.port.in;

import com.hanyahunya.task.application.port.command.CreateTaskCommand;
import com.hanyahunya.task.application.port.command.DeleteTaskCommand;
import com.hanyahunya.task.application.port.command.UpdateTaskActiveCommand;
import com.hanyahunya.task.application.port.command.UpdateTaskNameCommand;
import com.hanyahunya.task.application.port.response.TaskResponse;

import java.util.List;
import java.util.UUID;

public interface TaskUseCase {
    void createTask(CreateTaskCommand command);

    List<TaskResponse> getTask(UUID userId);

    void deleteTask(DeleteTaskCommand command);

    void updateTaskActive(UpdateTaskActiveCommand command);

    void updateTaskName(UpdateTaskNameCommand command);
}
