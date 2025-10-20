package com.hanyahunya.task.adapter.in.web.dto;

import jakarta.validation.Valid; // @Valid 임포트
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty; // @NotEmpty 임포트
import jakarta.validation.constraints.NotNull; // @NotNull 임포트

import java.util.List;
import java.util.Map;

public record CreateTaskRequest(
        @NotBlank
        String taskName,

        @Valid
        @NotNull
        TriggerRequest trigger,

        @Valid
        @NotEmpty
        List<ActionRequest> actions
) {
    public record TriggerRequest(
            @NotBlank
            String capabilityId,

            @NotNull
            Map<String, Object> config
    ) {}

    public record ActionRequest(
            @NotBlank
            String capabilityId,

            @NotNull
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