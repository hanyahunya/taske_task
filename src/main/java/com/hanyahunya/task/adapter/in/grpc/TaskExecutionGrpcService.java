package com.hanyahunya.task.adapter.in.grpc;

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

            ExecutionDetailsResponse response = responseBuilder.build();

            responseObserver.onNext(response);
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
     */
    private ModuleCapabilityInfo buildCapabilityInfo(ModuleCapability capability) {
        // [수정] 원본 paramSchema를 Struct로 변환
        Map<String, Object> paramSchema = capability.getParamSchema();

        // [수정] 원본 outputSchema를 가져오게 변경
        Map<String, Object> outputSchema = capability.getOutputSchema();

        // Map들을 Struct로 변환
        ModuleCapabilityInfo.Builder builder = ModuleCapabilityInfo.newBuilder()
                .setCapabilityId(capability.getCapabilityId())
                .setExecutionType(capability.getExecutionType().name())
                .setParamSchema(toStruct(paramSchema)) // [수정] 원본 paramSchema 전달
                .setExecutionSpec(toStruct(capability.getExecutionSpec())) // Struct
                .setOutputSchema(toStruct(outputSchema)) // [수정] 원본 outputSchema 전달
                .setModuleInfo(buildModuleInfo(capability.getModule()));
        return builder.build();
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