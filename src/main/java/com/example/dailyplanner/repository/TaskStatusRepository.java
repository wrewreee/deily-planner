package com.example.dailyplanner.repository;

import com.example.dailyplanner.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskStatusRepository extends JpaRepository<TaskStatus, String> {

    Optional<TaskStatus> findByName(String name);

}
