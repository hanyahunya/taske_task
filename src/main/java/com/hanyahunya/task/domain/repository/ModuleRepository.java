package com.hanyahunya.task.domain.repository;

import com.hanyahunya.task.domain.model.Module;
import com.hanyahunya.task.domain.model.ModuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, ModuleType> {
    @Query("SELECT m FROM Module m LEFT JOIN FETCH m.capabilities")
    List<Module> findAllWithCapabilities();
}
