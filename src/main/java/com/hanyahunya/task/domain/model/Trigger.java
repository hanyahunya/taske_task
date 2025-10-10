package com.hanyahunya.task.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Getter
@Builder
@Table(name = "triggers")
@NoArgsConstructor
@AllArgsConstructor
public class Trigger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trigger_id")
    private Long triggerId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capability_id", nullable = false)
    private ModuleCapability capability;

    /*
    - 빈칸 채우기 양식( capability ) 에서 정의한 param_schema
    {
        "properties": {
            "to": { "type": "string" },
            "subject": { "type": "string" }
        }
    }

    ### trigger_config ### 해당 양식에 실제로 들어갈 값들을 정의
    {
        "to": "friend@example.com",
        "subject": "새로운 YouTube 영상 알림!"
    }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "trigger_config", columnDefinition = "json")
    private Map<String, Object> triggerConfig;
}