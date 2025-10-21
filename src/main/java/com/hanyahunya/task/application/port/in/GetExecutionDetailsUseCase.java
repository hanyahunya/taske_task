package com.hanyahunya.task.application.port.in;

import com.hanyahunya.task.domain.model.Action;

import java.util.List;

public interface GetExecutionDetailsUseCase {
    List<Action> getExecutionDetails(Long taskId);
}
