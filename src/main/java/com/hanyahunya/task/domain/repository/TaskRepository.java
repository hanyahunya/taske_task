package com.hanyahunya.task.domain.repository;

import com.hanyahunya.task.domain.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
