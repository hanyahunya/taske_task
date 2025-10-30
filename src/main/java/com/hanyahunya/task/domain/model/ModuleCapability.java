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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "required_scopes", columnDefinition = "json")
    private List<String> requiredScopes; // ["value1", "value2", ...]

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_type", length = 20, nullable = false)
    private ExecutionType executionType; // HTTP_POLLING, WEBHOOK, HTTP_REQUEST 등

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "execution_spec", columnDefinition = "json")
    private Map<String, Object> executionSpec; // { "key1": "value1", ...}

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "param_schema", columnDefinition = "json")
    private Map<String, Object> paramSchema;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "output_schema", columnDefinition = "json")
    private Map<String, Object> outputSchema;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dependency", columnDefinition = "json")
    private List<Map<String, Object>> dependency;

    @Column(name = "is_dependency")
    private Boolean isDependency;
}