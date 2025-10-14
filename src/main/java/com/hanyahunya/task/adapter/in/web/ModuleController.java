package com.hanyahunya.task.adapter.in.web;

import com.hanyahunya.task.adapter.in.web.dto.ModuleResponse;
import com.hanyahunya.task.application.port.in.ModuleUseCase;
import com.hanyahunya.task.domain.model.Module;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleUseCase moduleUseCase;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_USER') or hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<ModuleResponse>> getModules() {
        List<Module> modules = moduleUseCase.getAllModules(); // read only -> dirty check safe

        List<ModuleResponse> response = modules.stream()
                .map(ModuleResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}