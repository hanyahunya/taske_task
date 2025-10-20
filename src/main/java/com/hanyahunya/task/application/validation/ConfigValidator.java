package com.hanyahunya.task.application.validation;

import com.hanyahunya.task.application.exception.InvalidConfigException;
import com.hanyahunya.task.domain.model.ModuleCapability;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class ConfigValidator {

    /**
     * ModuleCapability의 paramSchema를 기반으로 사용자 입력을 검증합니다.
     *
     * @param capability 검증의 기준이 되는 ModuleCapability
     * @param config     사용자가 입력한 설정값
     * @throws InvalidConfigException 검증 실패 시
     */
    public void validate(ModuleCapability capability, Map<String, Object> config) {
        Map<String, Object> paramSchema = capability.getParamSchema();

        // paramSchema가 없으면 검증할 것이 없음
        if (paramSchema == null || paramSchema.isEmpty()) {
            log.debug("Capability ID {}: paramSchema가 정의되지 않아 검증을 건너뜁니다.", capability.getCapabilityId());
            return;
        }

        // 1. 정의되지 않은 키 검증
        // paramSchema의 'properties'에 정의되지 않은 키가 config에 있는지 검증
        validateUndefinedKeys(capability, config, paramSchema);

        // 2. 필수(required) 필드 누락 및 빈 값 검증
        // paramSchema의 'required' 목록을 기반으로 config에 값이 누락되었거나 비어있는지 검증
        validateRequiredFields(capability, config, paramSchema);

        // 3. (TODO) 추후 상세 타입, 포맷, 길이 등을 검증하는 로직 추가
        // validateFieldFormats(capability, config, paramSchema);
    }

    /**
     * paramSchema의 'properties'에 정의되지 않은 키가 config에 있는지 검증
     */
    @SuppressWarnings("unchecked")
    private void validateUndefinedKeys(ModuleCapability capability, Map<String, Object> config, Map<String, Object> paramSchema) {
        Object propertiesObj = paramSchema.get("properties");
        if (!(propertiesObj instanceof Map)) {
            // 'properties' 스키마가 없으면, 알 수 없는 키 검증을 수행할 수 없음
            log.trace("Capability ID {}: properties 스키마가 없어 알 수 없는 키 검증을 건너뜁니다.", capability.getCapabilityId());
            return;
        }

        Map<String, Object> properties;
        try {
            properties = (Map<String, Object>) propertiesObj;
        } catch (ClassCastException e) {
            log.warn("Capability ID {}: 'properties' 스키마가 Map<String, Object> 형식이 아닙니다.", capability.getCapabilityId());
            return;
        }


        for (String configKey : config.keySet()) {
            if (!properties.containsKey(configKey)) {
                String errorMsg = String.format("Capability ID '%s': 정의되지 않은 설정 키가 포함되어 있습니다: '%s'",
                        capability.getCapabilityId(), configKey);
                log.warn(errorMsg);
                throw new InvalidConfigException(errorMsg);
            }
        }
    }

    /**
     * paramSchema의 'required' 목록을 기반으로 config에 값이 누락되었거나 비어있는지 검증
     */
    @SuppressWarnings("unchecked")
    private void validateRequiredFields(ModuleCapability capability, Map<String, Object> config, Map<String, Object> paramSchema) {
        Object requiredObj = paramSchema.get("required");
        if (!(requiredObj instanceof List)) {
            // 'required' 목록이 없으면 필수 필드 검증을 건너뜀
            log.trace("Capability ID {}: required 스키마가 없어 필수 필드 검증을 건너뜁니다.", capability.getCapabilityId());
            return;
        }

        List<String> requiredFields;
        try {
            requiredFields = (List<String>) requiredObj;
        } catch (ClassCastException e) {
            log.warn("Capability ID {}: 'required' 필드가 List<String> 형식이 아닙니다.", capability.getCapabilityId());
            return; // 스키마가 잘못되었으므로 검증 중단
        }

        for (String fieldName : requiredFields) {
            Object value = config.get(fieldName);

            // 1. 키 자체가 없는 경우
            if (!config.containsKey(fieldName)) {
                String errorMsg = String.format("Capability ID '%s': 필수 필드가 누락되었습니다: '%s'",
                        capability.getCapabilityId(), fieldName);
                log.warn(errorMsg);
                throw new InvalidConfigException(errorMsg);
            }

            // 2. 값이 비어있는 경우 (null, 빈 문자열, 빈 컬렉션/맵)
            // (참고: 숫자 0은 비어있는 것으로 간주하지 않음)
            if (isEmpty(value)) {
                String errorMsg = String.format("Capability ID '%s': 필수 필드 값이 비어있습니다: '%s'",
                        capability.getCapabilityId(), fieldName);
                log.warn(errorMsg);
                throw new InvalidConfigException(errorMsg);
            }
        }
    }

    /**
     * 값이 비어있는지 (null, 빈 문자열, 빈 컬렉션, 빈 맵) 확인
     */
    private boolean isEmpty(Object value) {
        if (Objects.isNull(value)) {
            return true;
        }
        if (value instanceof String str) {
            return str.isBlank();
        }
        if (value instanceof Collection<?> col) {
            return col.isEmpty();
        }
        if (value instanceof Map<?, ?> map) {
            return map.isEmpty();
        }
        return false;
    }
}