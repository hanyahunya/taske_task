package com.hanyahunya.task.application.port.in;

import com.hanyahunya.task.domain.model.Module;

import java.util.List;

public interface ModuleUseCase {
    List<Module> getAllModules();
}
