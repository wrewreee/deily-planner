package com.example.dailyplanner.controller;

import com.example.dailyplanner.dto.TaskRequest;
import com.example.dailyplanner.dto.TaskResponse;
import com.example.dailyplanner.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Операции по созданию, получению, фильтрации, обновлению и удалению задач")
public class TaskController {

    private final TaskService taskService;

    @Operation(
            summary = "Создать новую задачу",
            description = "Создаёт задачу для указанного пользователя с заданным статусом, приоритетом и сроком выполнения"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача успешно создана",
                    content = @Content(schema = @Schema(implementation = TaskResponse.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или некорректный запрос"),
            @ApiResponse(responseCode = "404", description = "Пользователь или статус не найден")
    })
    @PostMapping
    public TaskResponse createTask(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания задачи",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = TaskRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "title": "Подготовить диплом",
                                      "userId": "b3149c21-a07b-450f-9deb-2166ad1d6a68",
                                      "statusName": "NEW",
                                      "deadline": "2026-03-30",
                                      "priority": 5
                                    }
                                    """)
                    )
            )
            @Valid @RequestBody TaskRequest request
    ) {
        return taskService.createTask(request);
    }

    @Operation(
            summary = "Получить задачи пользователя",
            description = "Возвращает задачи указанного пользователя с поддержкой пагинации и сортировки"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список задач успешно получен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/user/{userId}")
    public Page<TaskResponse> getUserTasks(
            @Parameter(description = "Идентификатор пользователя", example = "b3149c21-a07b-450f-9deb-2166ad1d6a68")
            @PathVariable String userId,

            @Parameter(description = "Номер страницы, начиная с 0", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Поле сортировки", example = "deadline")
            @RequestParam(defaultValue = "deadline") String sortBy,

            @Parameter(description = "Направление сортировки: asc или desc", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return taskService.getUserTasks(userId, page, size, sortBy, sortDir);
    }

    @Operation(
            summary = "Фильтрация задач",
            description = "Возвращает задачи с учётом фильтров по пользователю, статусу, приоритету, названию и диапазону дат"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Фильтрация выполнена успешно"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры фильтрации")
    })
    @GetMapping("/filter")
    public Page<TaskResponse> filterTasks(
            @Parameter(description = "Идентификатор пользователя", example = "b3149c21-a07b-450f-9deb-2166ad1d6a68")
            @RequestParam(required = false) String userId,

            @Parameter(description = "Название статуса задачи", example = "NEW")
            @RequestParam(required = false) String statusName,

            @Parameter(description = "Приоритет задачи", example = "5")
            @RequestParam(required = false) Integer priority,

            @Parameter(description = "Часть названия задачи", example = "диплом")
            @RequestParam(required = false) String title,

            @Parameter(description = "Начальная дата диапазона дедлайна в формате yyyy-MM-dd", example = "2026-03-01")
            @RequestParam(required = false) String deadlineFrom,

            @Parameter(description = "Конечная дата диапазона дедлайна в формате yyyy-MM-dd", example = "2026-03-31")
            @RequestParam(required = false) String deadlineTo,

            @Parameter(description = "Номер страницы", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Размер страницы", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Поле сортировки", example = "priority")
            @RequestParam(defaultValue = "deadline") String sortBy,

            @Parameter(description = "Направление сортировки: asc или desc", example = "desc")
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return taskService.filterTasks(
                userId, statusName, priority, title, deadlineFrom, deadlineTo, page, size, sortBy, sortDir
        );
    }

    @Operation(
            summary = "Обновить название задачи",
            description = "Изменяет название существующей задачи по её идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Название задачи обновлено"),
            @ApiResponse(responseCode = "400", description = "Некорректное новое название"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @PutMapping("/{taskId}")
    public TaskResponse updateTitle(
            @Parameter(description = "Идентификатор задачи", example = "9a8f2f2b-0b4d-4b57-8a23-2a8dc9f2a111")
            @PathVariable String taskId,

            @Parameter(description = "Новое название задачи", example = "Обновить план диплома")
            @RequestParam @NotBlank(message = "Title must not be blank") String title
    ) {
        return taskService.updateTaskTitle(taskId, title);
    }

    @Operation(
            summary = "Удалить задачу",
            description = "Удаляет задачу по её идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача удалена"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @DeleteMapping("/{taskId}")
    public void deleteTask(
            @Parameter(description = "Идентификатор задачи", example = "9a8f2f2b-0b4d-4b57-8a23-2a8dc9f2a111")
            @PathVariable String taskId
    ) {
        taskService.deleteTask(taskId);
    }

    @Operation(
            summary = "Изменить статус задачи",
            description = "Изменяет статус задачи по её идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус задачи изменён"),
            @ApiResponse(responseCode = "400", description = "Некорректное имя статуса"),
            @ApiResponse(responseCode = "404", description = "Задача или статус не найдены")
    })
    @PutMapping("/{taskId}/status")
    public TaskResponse changeStatus(
            @Parameter(description = "Идентификатор задачи", example = "9a8f2f2b-0b4d-4b57-8a23-2a8dc9f2a111")
            @PathVariable String taskId,

            @Parameter(description = "Новое имя статуса", example = "IN_PROGRESS")
            @RequestParam @NotBlank(message = "Status must not be blank") String status
    ) {
        return taskService.changeStatus(taskId, status);
    }
}
