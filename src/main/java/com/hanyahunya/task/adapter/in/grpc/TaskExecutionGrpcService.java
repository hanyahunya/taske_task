package com.hanyahunya.task.adapter.in.grpc;

import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.hanyahunya.grpc.*;
import com.hanyahunya.task.application.port.in.GetExecutionDetailsUseCase;
import com.hanyahunya.task.domain.model.Action;
import com.hanyahunya.task.domain.model.Module;
import com.hanyahunya.task.domain.model.ModuleCapability;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

import java.util.HashMap; // HashMap import 추가
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hanyahunya.task.adapter.in.grpc.util.ToStructConverter.toStruct;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class TaskExecutionGrpcService extends TaskExecutionServiceGrpc.TaskExecutionServiceImplBase {

    private final GetExecutionDetailsUseCase getExecutionDetailsUseCase;

    @Override
    public void getExecutionDetails(ExecutionDetailsRequest request, StreamObserver<ExecutionDetailsResponse> responseObserver) {
        long taskId = request.getTaskId();
        log.info("gRPC GetExecutionDetails 요청 수신. Task ID: {}", taskId);

        try {
            List<Action> actions = getExecutionDetailsUseCase.getExecutionDetails(taskId);

            if (actions.isEmpty()) {
                log.warn("Task ID {}에 해당하는 Action이 없습니다. 빈 목록을 반환합니다.", taskId);
            }

            ExecutionDetailsResponse.Builder responseBuilder = ExecutionDetailsResponse.newBuilder();

            // Action -> gRPC DTO(ActionInfo)
            List<ActionInfo> actionInfos = actions.stream()
                    .map(this::buildActionInfo)
                    .collect(Collectors.toList());

            responseBuilder.addAllActions(actionInfos);

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Task ID {} 실행 정보 조회 중 오류 발생: {}", taskId, e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to retrieve execution details: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /**
     * Action Entity -> gRPC ActionInfo DTO
     */
    private ActionInfo buildActionInfo(Action action) {
        return ActionInfo.newBuilder()
                .setExecutionOrder(action.getExecutionOrder())
                .setCapabilityId(action.getCapability().getCapabilityId()) // (로깅용)
                .setActionConfig(toStruct(action.getActionConfig())) // Struct로 변환
                .setCapabilityInfo(buildCapabilityInfo(action.getCapability()))
                .build();
    }

    /**
     * ModuleCapability Entity -> gRPC ModuleCapabilityInfo DTO
     * --- ✅ [수정] outputSchema를 필터링하도록 변경 ---
     */
    private ModuleCapabilityInfo buildCapabilityInfo(ModuleCapability capability) {
        // 1. 원본 outputSchema Map을 가져옵니다.
        Map<String, Object> originalSchema = capability.getOutputSchema();

        // 2. "type" 정보만 갖도록 스키마를 필터링합니다.
        Map<String, Object> filteredSchema = filterOutputSchemaForType(originalSchema);

        // 3. 필터링된 Map을 Struct로 변환합니다.
        ModuleCapabilityInfo.Builder builder = ModuleCapabilityInfo.newBuilder()
                .setCapabilityId(capability.getCapabilityId())
                .setExecutionType(capability.getExecutionType().name())
                .setExecutionSpec(toStruct(capability.getExecutionSpec())) // Struct
                .setOutputSchema(toStruct(filteredSchema))
                .setModuleInfo(buildModuleInfo(capability.getModule()));
        return builder.build();
    }

    /**
     * OutputSchema를 "type" 정보만 갖도록 필터링하는 헬퍼 메서드
     * * @param originalSchema 원본 Map (예: {"success": {"name":"...", "type":"boolean", ...}})
     * @return 필터링된 Map (예: {"success": {"type":"boolean"}})
     */
    private Map<String, Object> filterOutputSchemaForType(Map<String, Object> originalSchema) {
        if (originalSchema == null || originalSchema.isEmpty()) {
            return originalSchema;
        }

        return originalSchema.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof Map) // 값이 Map인 항목(예: "success": {...})만 처리
                .collect(Collectors.toMap(
                        Map.Entry::getKey, // 키는 그대로 사용 (예: "success")
                        entry -> {
                            // 값(Value)은 내부 Map에서 "type"만 추출하여 새 Map 생성
                            Map<String, Object> propertyDetails = (Map<String, Object>) entry.getValue();
                            Object type = propertyDetails.get("type");

                            Map<String, Object> typeOnlyMap = new HashMap<>();
                            if (type != null) {
                                typeOnlyMap.put("type", type);
                            }
                            // (결과) -> {"type": "boolean"}
                            return typeOnlyMap;
                        }
                ));
    }


    /**
     * Module Entity -> gRPC ModuleInfo DTO
     */
    private ModuleInfo buildModuleInfo(Module module) {
        ModuleInfo.Builder builder = ModuleInfo.newBuilder()
                .setModuleId(module.getModuleId().name());

        if (module.getAuthType() != null) {
            builder.setAuthType(module.getAuthType().name());
        }
        if (module.getApiBaseUrl() != null) {
            builder.setApiBaseUrl(module.getApiBaseUrl());
        }
        return builder.build();
    }
}