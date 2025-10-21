package com.hanyahunya.kafkaDto;

import java.util.Map;
import java.util.UUID;

public record TriggerFiredEvent(
        UUID userId,
        Long taskId,
        /**
         * Trigger가 실행되면서 생성된 초기 데이터 (컨텍스트의 시작점).
         * 첫 번째 Action은 이 데이터를 {{trigger.output...}} 형태로 참조가능
         * {"videoId": "xyz-123", "title": "새로운 강의", ...} 이런식
         */
        Map<String, Object> triggerOutput
) {}
