package com.example.dailyplanner.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiOverloadedDayDto {

    private String date;
    private Integer taskCount;
    private Integer totalEstimatedMinutes;
    private String loadLevel;
}
