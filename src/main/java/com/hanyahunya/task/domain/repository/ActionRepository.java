package com.hanyahunya.task.domain.repository;

import com.hanyahunya.task.domain.model.Action;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionRepository extends JpaRepository<Action, Long> {
}
