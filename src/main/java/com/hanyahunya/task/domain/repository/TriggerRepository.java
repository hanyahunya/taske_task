package com.hanyahunya.task.domain.repository;

import com.hanyahunya.task.domain.model.Trigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TriggerRepository extends JpaRepository<Trigger, Long> {
    @Query("SELECT t FROM Trigger t " +
            "JOIN FETCH t.task task " +
            "JOIN FETCH t.capability cap " +
            "WHERE task.isActive = true AND cap.capabilityType = 'TRIGGER' AND cap.executionType = 'SCHEDULING'")
    List<Trigger> findTriggersByExecutionTypeSchedule();
}
