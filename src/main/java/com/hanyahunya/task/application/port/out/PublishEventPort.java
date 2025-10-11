package com.hanyahunya.task.application.port.out;

import com.hanyahunya.kafkaDto.TriggerFiredEvent;

public interface PublishEventPort {
    void publishTriggerFiredEvent(TriggerFiredEvent event);
}