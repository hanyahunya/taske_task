package com.hanyahunya.task.application.service;

import com.hanyahunya.task.application.port.in.ModuleUseCase;
import com.hanyahunya.task.domain.model.Module;
import com.hanyahunya.task.domain.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuleService implements ModuleUseCase {

    private final ModuleRepository moduleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Module> getAllModules() {
        return moduleRepository.findAllWithCapabilities();
    }
}