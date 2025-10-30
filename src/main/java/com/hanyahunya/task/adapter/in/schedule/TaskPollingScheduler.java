package com.hanyahunya.task.adapter.in.schedule;

import com.hanyahunya.task.application.port.command.FireTriggerCommand;
import com.hanyahunya.task.application.port.in.FireTriggerUseCase;
import com.hanyahunya.task.domain.model.Trigger;
import com.hanyahunya.task.domain.repository.TriggerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TaskPollingScheduler {
    private final TriggerRepository triggerRepository;
    private final FireTriggerUseCase fireTriggerUseCase;

    // 초 분 시 일 월 요알
    @Scheduled(cron = "0 * * * * *") // 매 분 0초마다 실행
    public void pollForScheduledTasks() {
        List<Trigger> scheduledTriggers = triggerRepository.findTriggersByExecutionTypeSchedule();

        // [변경] 스케쥴러가 43분 59초처럼 분 경계 직전에 실행되는 경우를 대비해,
        // 현재 시간에 2초의 여유(buffer)를 더한 '유효 시간(effectiveTime)'을 기준 시간으로 사용
        // 예: now()가 10:43:59.500 -> effectiveTime = 10:44:01.500
        LocalDateTime effectiveTime = LocalDateTime.now().plusSeconds(1);

        for (Trigger trigger : scheduledTriggers) {
            try {
                // 각 Trigger의 config 에서 cron 표현식을 가져옴
                String cronExpression = (String) trigger.getTriggerConfig().get("cron");
                if (cronExpression == null || cronExpression.isBlank()) {
                    continue; // cron 정보가 없으면 건너뜀
                }

                // cron 표현식 파싱
                CronExpression cron = CronExpression.parse(cronExpression);

                // [변경] 'now' 대신 'effectiveTime' 을 기준으로 1분 전을 계산합
                // 예: (10:42:59 ~ 10:43:59] -> (10:43:01 ~ 10:44:01]
                LocalDateTime lastMinute = effectiveTime.minusMinutes(1);
                LocalDateTime nextExecutionTime = cron.next(lastMinute);

                // [변경] 다음 실행 시간이 있고, 그 시간이 'now' 가 아닌 'effectiveTime' 보다 이전이거나 같으면 실행 대상으로 판단
                if (nextExecutionTime != null && !nextExecutionTime.isAfter(effectiveTime)) {

                    FireTriggerCommand command = new FireTriggerCommand(
                            trigger.getTask().getUserId(),
                            trigger.getTask().getTaskId(),
                            Map.of()
                    );

                    // 트리거 발동
                    fireTriggerUseCase.fireTrigger(command);
                }
            } catch (Exception e) {
                // to do 로깅
                System.err.println("Failed to process schedule for taskId: " + trigger.getTask().getTaskId() + " - " + e.getMessage());
            }
        }
    }
}