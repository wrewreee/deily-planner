package com.example.dailyplanner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "DTO ответа с данными задачи")
public class TaskResponse {

    @Schema(description = "Идентификатор задачи", example = "9a8f2f2b-0b4d-4b57-8a23-2a8dc9f2a111")
    private String id;

    @Schema(description = "Название задачи", example = "Подготовить диплом")
    private String title;

    @Schema(description = "Текущий статус задачи", example = "NEW")
    private String status;

    @Schema(description = "Идентификатор пользователя", example = "b3149c21-a07b-450f-9deb-2166ad1d6a68")
    private String userId;

    @Schema(description = "Приоритет задачи", example = "5")
    private Integer priority;
}
