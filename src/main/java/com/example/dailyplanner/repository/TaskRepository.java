package com.example.dailyplanner.repository;

import com.example.dailyplanner.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TaskRepository extends JpaRepository<Task, String>, JpaSpecificationExecutor<Task> {
}
