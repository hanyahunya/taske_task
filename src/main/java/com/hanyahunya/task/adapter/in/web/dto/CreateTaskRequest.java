package com.hanyahunya.task.adapter.in.web.dto;

import java.util.List;
import java.util.Map;

public record CreateTaskRequest(
        String taskName,
        TriggerRequest trigger,
        List<ActionRequest> actions
) {
    public record TriggerRequest(
            String capabilityId,
            Map<String, Object> config
    ) {}

    public record ActionRequest(
            String capabilityId,
            Map<String, Object> config
    ) {}
}

/*
요런식으로 추가
    {
        "taskName": "새로운 YouTube 영상 올라오면 이메일 보내기",
        "trigger": {
            "capabilityId": "youtube-new-video-in-channel",
            "config": {
                "channelId": "UC-채널Id"
            }
        },
        "actions": [
            {
                "capabilityId": "gmail-send-email",
                "config": {
                    "to": "hanyahunya@example.com",
                    "subject": "새로운 YouTube 영상 알림: {{trigger.output.title}}",
                    "body": "방금 새로운 영상이 올라왔어요! 지금 확인해보세요: {{trigger.output.videoUrl}}"
                }
            }
        ]
    }
 */