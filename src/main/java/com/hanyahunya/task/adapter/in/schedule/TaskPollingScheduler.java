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

        LocalDateTime now = LocalDateTime.now();

        for (Trigger trigger : scheduledTriggers) {
            try {
                // 각 Trigger의 config 에서 cron 표현식을 가져옴
                String cronExpression = (String) trigger.getTriggerConfig().get("cron");
                if (cronExpression == null || cronExpression.isBlank()) {
                    continue; // cron 정보가 없으면 건너뜀
                }

                // cron 표현식을 파싱, 현재 시간과 일치하는지 확인
                CronExpression cron = CronExpression.parse(cronExpression);

                // 이전 1분 전을 기준으로 다음 실행 시간을 계산해야, 현재 분에 실행되어야 할 작업을 놓치지 않기에
                LocalDateTime lastMinute = now.minusMinutes(1);
                LocalDateTime nextExecutionTime = cron.next(lastMinute);

                // 다음 실행 시간이 있고, 그 시간이 현재 시간보다 이전이거나 같으면 실행 대상
                if (nextExecutionTime != null && !nextExecutionTime.isAfter(now)) {

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