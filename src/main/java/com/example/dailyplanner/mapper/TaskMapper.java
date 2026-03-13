package com.example.dailyplanner.mapper;

import com.example.dailyplanner.dto.TaskResponse;
import com.example.dailyplanner.model.Task;

public class TaskMapper {

    public static TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .status(task.getStatus().getName())
                .userId(task.getUser().getId())
                .priority(task.getPriority())
                .build();
    }

}
