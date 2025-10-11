package com.hanyahunya.task.adapter.out.kafka;

import com.hanyahunya.kafkaDto.TriggerFiredEvent;
import com.hanyahunya.task.application.port.out.PublishEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TriggerEventKafkaAdapter implements PublishEventPort {

    private final KafkaTemplate<String, TriggerFiredEvent> kafkaTemplate;
    private static final String TOPIC = "trigger-fired-events";

    @Override
    public void publishTriggerFiredEvent(TriggerFiredEvent event) {
        // 실제 Kafka로 이벤트를 발행하는 로직
        kafkaTemplate.send(TOPIC, event.taskId().toString(), event);
    }
}