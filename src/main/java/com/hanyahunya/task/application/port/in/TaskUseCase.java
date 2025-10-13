package com.hanyahunya.task.application.port.in;

import com.hanyahunya.task.application.port.command.CreateTaskCommand;

public interface TaskUseCase {
    void createTask(CreateTaskCommand command);
}
