package com.example.dailyplanner.repository;

import com.example.dailyplanner.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}