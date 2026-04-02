package com.example.dailyplanner.repository;

import com.example.dailyplanner.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TaskStatusRepository extends JpaRepository<TaskStatus, UUID> {

    Optional<TaskStatus> findByName(String name);
}
