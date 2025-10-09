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
@Table(name = "works")
@NoArgsConstructor
@AllArgsConstructor
public class Work {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "work_id")
    private Long workId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Task task;

    @Column(name = "execution_order", nullable = false)
    private int executionOrder;

    @Column(name = "work_type", length = 50, nullable = false)
    private String workType; // todo 어떻게 설정할지

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "work_config", columnDefinition = "json")
    private Map<String, Object> workConfig;
}