package com.hanyahunya.task.application.validation;

import com.hanyahunya.task.application.exception.InvalidConfigException;
import com.hanyahunya.task.application.port.command.CreateTaskCommand;
import com.hanyahunya.task.domain.model.Action;
import com.hanyahunya.task.domain.model.ModuleCapability;
import com.hanyahunya.task.domain.model.Task;
import com.hanyahunya.task.domain.repository.ModuleCapabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Action의 의존성을 해결하고,
 * 실행 순서(executionOrder) 및 변수 참조(#_actionN...%_#)를 재조정하는 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DependencyResolver {

    private final ModuleCapabilityRepository moduleCapabilityRepository;
    private final ConfigValidator configValidator;

    // Action 변수 참조를 위한 정규식
    private static final Pattern ACTION_VARIABLE_PATTERN = Pattern.compile("#_%action(\\d+)\\.output\\.(.+?)%_#");

    // 시스템이 주입하는 의존성 변수임을 나타내는 임시 고유 토큰
    private static final String DEP_TOKEN_PREFIX = "__%DEP_ACTION_";
    private static final String DEP_TOKEN_SUFFIX = "%__";


    /**
     * [내부 클래스] 의존성 처리 결과를 담는 DTO
     */
    private record DependencyResolutionResult(
            List<Action.ActionBuilder> dependencyActions,
            Map<String, Object> modifiedParentConfig,
            int dependenciesAdded
    ) {
        static DependencyResolutionResult noDependencies(Map<String, Object> originalConfig) {
            return new DependencyResolutionResult(Collections.emptyList(), originalConfig, 0);
        }
    }

    /**
     * ActionCommand 목록을 받아 의존성을 해결하고,
     * 최종 Action 리스트를 빌드하여 반환 (3-Pass Algorithm)
     */
    public List<Action> resolveAndBuildActions(Task savedTask, List<CreateTaskCommand.ActionCommand> originalActionCommands) {

        // [Pass 1] 의존성 주입 및 Action 빌더 생성, 인덱스 매핑
        List<Action.ActionBuilder> pendingActionBuilders = new ArrayList<>();
        Map<Integer, Integer> originalIndexToNewIndexMap = new HashMap<>();
        int currentExecutionIndex = 0;

        for (int i = 0; i < originalActionCommands.size(); i++) {
            CreateTaskCommand.ActionCommand actionCommand = originalActionCommands.get(i);
            ModuleCapability capability = moduleCapabilityRepository.findById(actionCommand.capabilityId())
                    .orElseThrow(() -> {
                        log.warn("잘못된 Action Capability ID 요청: {}", actionCommand.capabilityId());
                        return new IllegalArgumentException("Invalid action capability id: " + actionCommand.capabilityId());
                    });

            // 1. 원본 Action에 대한 config를 검증 ( 처음 사용자가 잘 입력했는지에 대한 검증 )
            log.debug("Task ID {}: Action {} (원본 인덱스: {}) *원본* config 검증 시작...", savedTask.getTaskId(), i, i);
            configValidator.validate(capability, actionCommand.config());
            log.debug("Task ID {}: Action {} (원본 인덱스: {}) *원본* config 검증 완료.", savedTask.getTaskId(), i, i);

            // 1. 재귀적으로 의존성 해결 (배열/단일 객체 모두 처리)
            log.debug("Task ID {}: Action {} (Capability: {}) 의존성 해결 시작...", savedTask.getTaskId(), i, capability.getCapabilityId());
            DependencyResolutionResult resolutionResult = resolveDependencies(
                    capability,
                    actionCommand.config(),
                    savedTask,
                    currentExecutionIndex // 현재 의존성이 시작될 실행 순서
            );

            // 2. 반환된 의존성 Action 빌더 목록을 추가
            pendingActionBuilders.addAll(resolutionResult.dependencyActions());
            currentExecutionIndex += resolutionResult.dependenciesAdded();
            log.debug("Task ID {}: Action {}에 대해 {}개의 의존성 Action 추가. (현재 executionIndex: {})",
                    savedTask.getTaskId(), i, resolutionResult.dependenciesAdded(), currentExecutionIndex);

            // 3. 원본 Action 빌더 생성 및 추가
            Map<String, Object> modifiedConfig = resolutionResult.modifiedParentConfig();

            Action.ActionBuilder originalActionBuilder = Action.builder()
                    .task(savedTask)
                    .capability(capability)
                    .actionConfig(modifiedConfig)
                    .executionOrder(currentExecutionIndex);

            pendingActionBuilders.add(originalActionBuilder);

            // 4. 원본 인덱스(i)가 어떤 새 실행 순서(currentExecutionIndex)에 매핑되는지 기록
            originalIndexToNewIndexMap.put(i, currentExecutionIndex);

            currentExecutionIndex++;
        }

        // [Pass 2 & 3] Config 변수 재조정 및 의존성 토큰 치환
        List<Action> finalActions = new ArrayList<>();
        for (Action.ActionBuilder builder : pendingActionBuilders) {
            Action tempAction = builder.build();
            Map<String, Object> configToProcess = tempAction.getActionConfig();

            // [Pass 2] Config 내부의 *사용자* 변수(#_%actionN...%_#)를 새 인덱스로 재조정
            Map<String, Object> renumberedConfig = renumberConfigVariables(
                    configToProcess,
                    originalIndexToNewIndexMap
            );

            // [Pass 3] Config 내부의 *시스템* 변수(__%DEP_ACTION...%__)를 최종 형식으로 치환
            Map<String, Object> finalConfig = replaceDependencyTokens(renumberedConfig);

            builder.actionConfig(finalConfig);
            finalActions.add(builder.build());
        }

        return finalActions;
    }


    /**
     * 재귀적으로 의존성을 해결
     * (dependency 필드가 List<Map> 형식으로 변경됨)
     */
    @SuppressWarnings("unchecked")
    private DependencyResolutionResult resolveDependencies(
            ModuleCapability parentCapability,
            Map<String, Object> parentConfig,
            Task task,
            int baseExecutionOrder
    ) {
        // 1. 의존성 정의 (List<Map<String, Object>>) 로딩
        List<Map<String, Object>> dependencyList = parentCapability.getDependency();

        // 2. 의존성 목록이 비어있는지 확인
        if (dependencyList == null || dependencyList.isEmpty()) {
            return DependencyResolutionResult.noDependencies(parentConfig);
        }

        // --- 의존성 배열을 순차적으로 처리하는 로직 ---
        List<Action.ActionBuilder> cumulativeDependencyActions = new ArrayList<>();
        Map<String, Object> configForNextStep = new HashMap<>(parentConfig);
        int cumulativeAddedCount = 0;
        int currentBaseOrder = baseExecutionOrder;

        // 3. 의존성 목록 [dep1, dep2, ...]을 순회
        for (Map<String, Object> dependencyMap : dependencyList) {
            try {
                // 4. 단일 의존성 정보 파싱
                String depCapabilityId = (String) dependencyMap.get("capability_id");
                Map<String, String> inputsMap = (Map<String, String>) dependencyMap.get("inputs");
                Map<String, String> outputsMap = (Map<String, String>) dependencyMap.get("outputs");

                if (depCapabilityId == null || inputsMap == null || outputsMap == null) {
                    log.warn("Capability ID {}: 'dependency' 맵 구조가 잘못되었습니다.", parentCapability.getCapabilityId());
                    continue;
                }

                ModuleCapability depCapability = moduleCapabilityRepository.findById(depCapabilityId)
                        .orElseThrow(() -> {
                            log.warn("의존성 Capability ID '{}'를 찾을 수 없습니다.", depCapabilityId);
                            return new IllegalArgumentException("Invalid dependency capability id: " + depCapabilityId);
                        });

                // 5. 이 의존성 모듈이 사용할 config (depConfig)를 구성
                Map<String, Object> depConfig = new HashMap<>();
                for (Map.Entry<String, String> entry : inputsMap.entrySet()) {
                    String depKey = entry.getKey();
                    String parentKey = entry.getValue();
                    if (configForNextStep.containsKey(parentKey)) {
                        depConfig.put(depKey, configForNextStep.get(parentKey));
                    }
                }

                // 6. [재귀 호출] 이 의존성 모듈의 *중첩* 의존성을 해결
                DependencyResolutionResult nestedResult = resolveDependencies(
                        depCapability,
                        depConfig,
                        task,
                        currentBaseOrder
                );

                // 7. 중첩 의존성 Action들을 최종 목록에 추가
                cumulativeDependencyActions.addAll(nestedResult.dependencyActions());
                int nestedAddedCount = nestedResult.dependenciesAdded();

                // 8. 현재 의존성 Action 빌더를 생성
                int currentDependencyOrder = currentBaseOrder + nestedAddedCount;
                Map<String, Object> currentDependencyConfig = nestedResult.modifiedParentConfig();

                configValidator.validate(depCapability, currentDependencyConfig);

                Action.ActionBuilder currentDependencyActionBuilder = Action.builder()
                        .task(task)
                        .capability(depCapability)
                        .actionConfig(currentDependencyConfig)
                        .executionOrder(currentDependencyOrder);

                cumulativeDependencyActions.add(currentDependencyActionBuilder);

                // 9. 다음 의존성 또는 부모 Action이 사용할 config(configForNextStep)를 업데이트


                // 9a. 'inputs' 키를 부모 config에서 제거 <- 로깅등 필요시 넣어도 상관없음. ( 어차피 안쓰기에 )
                for (String parentKey : inputsMap.values()) {
                    configForNextStep.remove(parentKey);
                }

                // 9b. 'outputs'를 참조하는 임시 토큰을 부모 config에 추가
                for (Map.Entry<String, String> entry : outputsMap.entrySet()) {
                    String parentKey = entry.getKey();
                    String depOutputKey = entry.getValue();

                    // (예: "__%DEP_ACTION_0.output.x%__")
                    String dynamicVariable = String.format("%s%d.output.%s%s",
                            DEP_TOKEN_PREFIX,         // "__%DEP_ACTION_"
                            currentDependencyOrder,   // (예: 0)
                            depOutputKey,             // (예: "x")
                            DEP_TOKEN_SUFFIX          // "%__"
                    );
                    configForNextStep.put(parentKey, dynamicVariable);
                }

                // 10. 다음 루프를 위한 순서 및 카운트 업데이트
                int totalActionsAddedForThisDep = nestedAddedCount + 1;
                cumulativeAddedCount += totalActionsAddedForThisDep;
                currentBaseOrder += totalActionsAddedForThisDep;

            } catch (ClassCastException e) {
                log.error("Capability ID {}: 'dependency' 맵 파싱 중 오류 발생.", parentCapability.getCapabilityId(), e);
                throw new InvalidConfigException("Failed to parse dependency map for capability: " + parentCapability.getCapabilityId());
            } catch (Exception e) {
                log.error("Capability ID {}: 의존성 처리 중 알 수 없는 오류 발생", parentCapability.getCapabilityId(), e);
                throw e;
            }
        } // --- 의존성 배열 for 루프 종료 ---

        // 11. 최종 결과 반환
        return new DependencyResolutionResult(
                cumulativeDependencyActions,
                configForNextStep,
                cumulativeAddedCount
        );
    }

    /**
     * [Pass 2] Config Map 내부의 사용자 Action 참조 변수(#_%actionN...%_#)를 재조정
     */
    private Map<String, Object> renumberConfigVariables(
            Map<String, Object> config,
            Map<Integer, Integer> originalIndexToNewIndexMap
    ) {
        if (config == null || config.isEmpty()) {
            return config;
        }

        Map<String, Object> renumberedMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            renumberedMap.put(entry.getKey(), renumberConfigValue(entry.getValue(), originalIndexToNewIndexMap));
        }
        return renumberedMap;
    }

    /**
     * [Pass 2] Config 값 (String, Map, List)을 재귀적으로 탐색하며 사용자 변수를 재조정
     */
    @SuppressWarnings("unchecked")
    private Object renumberConfigValue(
            Object value,
            Map<Integer, Integer> originalIndexToNewIndexMap
    ) {
        if (value instanceof String strValue) {
            Matcher matcher = ACTION_VARIABLE_PATTERN.matcher(strValue);
            StringBuffer sb = new StringBuffer();
            boolean found = false;

            while (matcher.find()) {
                found = true;
                int originalIndex = Integer.parseInt(matcher.group(1));
                String variableName = matcher.group(2);

                Integer newIndex = originalIndexToNewIndexMap.get(originalIndex);

                if (newIndex == null) {
                    log.warn("Action 변수 재조정 중 매핑을 찾을 수 없습니다. 원본 인덱스: {}", originalIndex);
                    matcher.appendReplacement(sb, matcher.group(0));
                } else {
                    String replacement = String.format("#_%%action%d.output.%s%%_#", newIndex, variableName);
                    matcher.appendReplacement(sb, replacement);
                }
            }
            matcher.appendTail(sb);
            return found ? sb.toString() : strValue;

        } else if (value instanceof Map) {
            return renumberConfigVariables((Map<String, Object>) value, originalIndexToNewIndexMap);

        } else if (value instanceof List) {
            List<?> originalList = (List<?>) value;
            // List는 수정 불가능할 수 있으므로 새로운 List로 생성
            return originalList.stream()
                    .map(item -> renumberConfigValue(item, originalIndexToNewIndexMap))
                    .collect(Collectors.toList());
        }
        return value;
    }


    /**
     * [신규 Pass 3] Config Map 내부의 시스템 의존성 토큰(__%DEP_...%__)을
     * 최종 Action 변수 형식(#_%actionN...%_#)으로 치환하는 메서드
     */
    private Map<String, Object> replaceDependencyTokens(Map<String, Object> config) {
        if (config == null || config.isEmpty()) {
            return config;
        }

        Map<String, Object> replacedMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            replacedMap.put(entry.getKey(), replaceDependencyTokenValue(entry.getValue()));
        }
        return replacedMap;
    }

    /**
     * [Pass 3] Config 값 (String, Map, List)을 재귀적으로 탐색하며 시스템 토큰을 치환하는 메서드
     */
    @SuppressWarnings("unchecked")
    private Object replaceDependencyTokenValue(Object value) {
        if (value instanceof String strValue) {
            // (예: "__%DEP_ACTION_0.output.x%__")
            if (strValue.startsWith(DEP_TOKEN_PREFIX) && strValue.endsWith(DEP_TOKEN_SUFFIX)) {
                // "0.output.x" 부분만 추출
                String innerContent = strValue.substring(DEP_TOKEN_PREFIX.length(), strValue.length() - DEP_TOKEN_SUFFIX.length());

                // (예: "#_%action0.output.x%_#")
                return String.format("#_%%action%s%%_#", innerContent);
            }
            // 그 외 문자열은 그대로 반환
            return strValue;

        } else if (value instanceof Map) {
            return replaceDependencyTokens((Map<String, Object>) value);

        } else if (value instanceof List) {
            List<?> originalList = (List<?>) value;
            return originalList.stream()
                    .map(this::replaceDependencyTokenValue)
                    .collect(Collectors.toList());
        }

        // 그 외 타입(숫자, 불리언 등)은 그대로 반환
        return value;
    }
}