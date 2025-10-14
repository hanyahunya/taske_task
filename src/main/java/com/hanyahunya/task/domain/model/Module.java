package com.hanyahunya.task.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@Table(name = "modules")
@NoArgsConstructor
@AllArgsConstructor
public class Module {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "module_id")
    private ModuleType moduleId;

    @Column(name = "module_name", length = 100, nullable = false)
    private String moduleName;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", length = 20)
    private AuthType authType;

    @Column(name = "api_base_url")
    private String apiBaseUrl;

    // Module을 조회할 때 연관된 capabilities를 함께 조회하기 위해 양방향으로 알려줌
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ModuleCapability> capabilities = new ArrayList<>();
}