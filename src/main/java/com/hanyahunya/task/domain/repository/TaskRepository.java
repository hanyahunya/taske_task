package com.hanyahunya.task.domain.repository;

import com.hanyahunya.task.domain.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByUserIdOrderByTaskIdDesc(UUID userId);

    @Modifying
    void deleteByTaskIdAndUserId(Long taskId, UUID userId);

    Optional<Task> findByTaskIdAndUserId(Long taskId, UUID userId);
}
