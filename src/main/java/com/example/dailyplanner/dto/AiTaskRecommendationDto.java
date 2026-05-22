package com.example.dailyplanner.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiTaskRecommendationDto {

    private String taskId;
    private String title;
    private String currentStatus;
    private Integer currentPriority;
    private Integer recommendedPriority;
    private String deadline;
    private String reason;
}
