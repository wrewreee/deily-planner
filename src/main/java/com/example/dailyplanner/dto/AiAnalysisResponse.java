package com.example.dailyplanner.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AiAnalysisResponse {

    private String userId;
    private String generatedAt;
    private Integer totalTasks;
    private Integer activeTasks;
    private List<AiTaskRecommendationDto> recommendations;
    private List<AiOverloadedDayDto> overloadedDays;
    private String summary;
}
