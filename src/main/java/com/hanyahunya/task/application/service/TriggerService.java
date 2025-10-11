package com.hanyahunya.task.application.service;

import com.hanyahunya.kafkaDto.TriggerFiredEvent;
import com.hanyahunya.task.application.port.in.FireTriggerUseCase;
import com.hanyahunya.task.application.port.out.PublishEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TriggerService implements FireTriggerUseCase {

    private final PublishEventPort publishEventPort;

    @Override
    public void fireTrigger(Long taskId, Map<String, Object> triggerData) {
        // Out-Port를 통해 DB에서 Task 정보 조회
//        Task task = loadTriggerPort.loadTaskByTrigger(taskId, capabilityId);

//        TriggerFiredEvent event = new TriggerFiredEvent(
//                task.getUserId(),
//                task.getTaskId(),
//                triggerData // Trigger 발동으로 얻은 실제 결과 데이터
//        );
        TriggerFiredEvent event = new TriggerFiredEvent(
                UUID.randomUUID(),
                1L,
                triggerData // Trigger 발동으로 얻은 실제 결과 데이터
        );

        publishEventPort.publishTriggerFiredEvent(event);
    }
}