package com.hanyahunya.task.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;

@Entity
@Getter
@Builder
@Table(
        name = "module_capabilities",
        indexes = {
                @Index(name = "idx_capability_type_execution_type", columnList = "capability_type, execution_type")
        }
)
@NoArgsConstructor
@AllArgsConstructor
public class ModuleCapability {

    @Id
    @Column(name = "capability_id", length = 100)
    private String capabilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @Enumerated(EnumType.STRING)
    @Column(name = "capability_type", length = 10, nullable = false)
    private CapabilityType capabilityType; // TRIGGER, ACTION

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // 처음에 유저가 등록할때만 사용
    /*
        ["https://www.googleapis.com/auth/youtube.readonly", "https://www.googleapis.com/auth/gmail.send",...]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "required_scopes", columnDefinition = "json")
    private List<String> requiredScopes; // ["value1", "value2", ...]


    @Enumerated(EnumType.STRING)
    @Column(name = "execution_type", length = 20, nullable = false)
    private ExecutionType executionType; // HTTP_POLLING, WEBHOOK, HTTP_REQUEST 등

    // worker 서비스에 전송할 필요 있음 엔드포인트 + method
    /*
        - TRIGGER EXAMPLE: 이 트리거가 어떻게 동작하는지.
        {
            "type": "HTTP_POLLING",             ### type 컬럼 제거 -> json 밖에 따로 enum 컬럼으로 재정의
            "cron": "*./5 * * * *",
            "endpoint": "/youtube/v3/activities",
            "method": "GET"
        }
        =========================================================================
        - ACTION EXAMPLE: 이 Action이 어떻게 동작하는지
        {
            "type": "HTTP_REQUEST",             ### type 컬럼 제거 -> json 밖에 따로 enum 컬럼으로 재정의
            "endpoint": "/gmail/v1/users/me/messages/send",
            "method": "POST"
        }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "execution_spec", columnDefinition = "json")
    private Map<String, Object> executionSpec; // { "key1": "value1", ...}

    //
    /*
        - TRIGGER EXAMPLE 이 트리거를 설정할때 사용자에게 어떤 정보를 입력받아야 하는지.
        {
            "type": "object",
            "properties": {
                "channelId": {
                "type": "string",
                "description": "알림을 받을 YouTube 채널의 ID"
                }
            },
            "required": ["channelId"]    // 이 값을 반드시 입력해야합니다. 라는뜻
        }
        =========================================================================
        - ACTION EXAMPLE: 해당 Action을 하기위해 어떤 정보를 입력받아야 하는지
            -> 이전 단계의 출력값( {{...}} ) 도 입력으로 사용가능 -> Action의 우선순위등 조합가능
        {
              "type": "object",
              "properties": {
                    "to": { "type": "string", "description": "받는 사람 이메일 주소" },
                    "subject": { "type": "string", "description": "이메일 제목" },
                    "body": { "type": "string", "description": "이메일 본문" }
              },
              "required": ["to", "subject", "body"]
        }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "param_schema", columnDefinition = "json")
    private Map<String, Object> paramSchema;

    /*
        - TRIGGER EXAMPLE: Trigger가 성공적으로 실행되었을때 어떤 데이터를 출력하는지              ### for HTTP_POLLING
            -> 다음 Action 단계에서 {{trigger.output.videoId}} 처럼 결과값 참조시 쓰일 예정
        {
            "type": "object",
            "properties": {
                "videoId": { "type": "string" },
                "title": { "type": "string" },
                "publishedAt": { "type": "string" },
                "videoUrl": { "type": "string" }
            }
        }
        =========================================================================
        - ACTION EXAMPLE: 해당 Action이 성공했을때 어떤 데이터를 출력하는지 ( 액션 모듈 끼리 상호작용시 도움됨 )
        {
            "type": "object",
            "properties": {
                "messageId": { "type": "string" },
                "threadId": { "type": "string" }
            }
        }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "output_schema", columnDefinition = "json")
    private Map<String, Object> outputSchema;
}