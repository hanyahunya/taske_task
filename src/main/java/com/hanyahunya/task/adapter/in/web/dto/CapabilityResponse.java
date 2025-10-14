package com.hanyahunya.task.adapter.in.web.dto;

import com.hanyahunya.task.domain.model.ExecutionType;
import com.hanyahunya.task.domain.model.ModuleCapability;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public class CapabilityResponse {

    private final String capabilityId;
    private final String name;
    private final String description;
    private final ExecutionType executionType;
    private final Map<String, Object> paramSchema;
    private final Map<String, Object> outputSchema;

    public static CapabilityResponse from(ModuleCapability capability) {
        return new CapabilityResponse(
                capability.getCapabilityId(),
                capability.getName(),
                capability.getDescription(),
                capability.getExecutionType(),
                capability.getParamSchema(),
                capability.getOutputSchema()
        );
    }
}