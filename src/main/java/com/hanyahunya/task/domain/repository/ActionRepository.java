package com.hanyahunya.task.domain.repository;

import com.hanyahunya.task.domain.model.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActionRepository extends JpaRepository<Action, Long> {
    @Query("SELECT a FROM Action a " +
            "JOIN FETCH a.capability cap " +
            "JOIN FETCH cap.module " +
            "WHERE a.task.taskId = :taskId " +
            "ORDER BY a.executionOrder ASC")
    List<Action> findAllWithCapabilityAndModuleByTaskId(@Param("taskId") Long taskId);
}
