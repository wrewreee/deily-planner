package com.example.dailyplanner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO для создания новой задачи")
public class TaskRequest {

    @Schema(description = "Название задачи", example = "Подготовить диплом", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Task title must not be blank")
    private String title;

    @Schema(description = "Идентификатор пользователя", example = "b3149c21-a07b-450f-9deb-2166ad1d6a68", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "User id must not be blank")
    private String userId;

    @Schema(description = "Название статуса задачи", example = "NEW", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Status name must not be blank")
    private String statusName;

    @Schema(description = "Срок выполнения в формате yyyy-MM-dd", example = "2026-03-30")
    private String deadline;

    @Schema(description = "Приоритет задачи от 1 до 10", example = "5")
    @Min(value = 1, message = "Priority must be at least 1")
    @Max(value = 10, message = "Priority must be at most 10")
    private Integer priority;
}
