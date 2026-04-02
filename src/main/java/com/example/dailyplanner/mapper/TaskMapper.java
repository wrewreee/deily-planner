package com.example.dailyplanner.mapper;

import com.example.dailyplanner.dto.TaskResponse;
import com.example.dailyplanner.model.Task;

public class TaskMapper {

    public static TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId() != null ? task.getId().toString() : null)
                .title(task.getTitle())
                .status(task.getStatus() != null ? task.getStatus().getName() : null)
                .userId(task.getUser() != null && task.getUser().getId() != null ? task.getUser().getId().toString() : null)
                .priority(task.getPriority())
                .build();
    }
}
