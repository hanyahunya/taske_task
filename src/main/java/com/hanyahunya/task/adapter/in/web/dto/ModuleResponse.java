package com.hanyahunya.task.adapter.in.web.dto;

import com.hanyahunya.task.domain.model.CapabilityType;
import com.hanyahunya.task.domain.model.Module;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class ModuleResponse {

    private final String moduleId;
    private final String moduleName;
    private final List<CapabilityResponse> triggers;
    private final List<CapabilityResponse> actions;

    public static ModuleResponse from(Module module) {
        // TRIGGER 타입의 Capability만 필터링하여 DTO 리스트로 변환
        List<CapabilityResponse> triggers = module.getCapabilities().stream()
                .filter(capability -> capability.getCapabilityType() == CapabilityType.TRIGGER)
                .map(CapabilityResponse::from)
                .collect(Collectors.toList());

        // ACTION 타입의 Capability만 필터링하여 DTO 리스트로 변환
        List<CapabilityResponse> actions = module.getCapabilities().stream()
                .filter(capability -> capability.getCapabilityType() == CapabilityType.ACTION)
                .map(CapabilityResponse::from)
                .collect(Collectors.toList());

        // 최종적으로 ModuleResponse DTO를 생성하여 반환
        return new ModuleResponse(
                module.getModuleId().name(),
                module.getModuleName(),
                triggers,
                actions
        );
    }
}