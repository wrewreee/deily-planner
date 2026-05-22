package com.example.dailyplanner.service;

import com.example.dailyplanner.dto.AiAnalysisResponse;
import com.example.dailyplanner.dto.AiOverloadedDayDto;
import com.example.dailyplanner.dto.AiTaskRecommendationDto;
import com.example.dailyplanner.exception.BadRequestException;
import com.example.dailyplanner.exception.ResourceNotFoundException;
import com.example.dailyplanner.model.Task;
import com.example.dailyplanner.model.User;
import com.example.dailyplanner.repository.TaskRepository;
import com.example.dailyplanner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskAiService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public AiAnalysisResponse analyzeUserTasks(String userId) {
        UUID parsedUserId = parseUuid(userId, "userId");

        User user = userRepository.findById(parsedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        List<Task> tasks = taskRepository.findByUserId(parsedUserId);

        List<Task> activeTasks = tasks.stream()
                .filter(task -> task.getStatus() == null || !"DONE".equalsIgnoreCase(task.getStatus().getName()))
                .toList();

        List<AiTaskRecommendationDto> recommendations = buildRecommendations(activeTasks);
        List<AiOverloadedDayDto> overloadedDays = buildOverloadedDays(activeTasks);

        String summary = buildSummary(user, tasks.size(), activeTasks.size(), recommendations, overloadedDays);

        return AiAnalysisResponse.builder()
                .userId(user.getId().toString())
                .generatedAt(LocalDateTime.now().toString())
                .totalTasks(tasks.size())
                .activeTasks(activeTasks.size())
                .recommendations(recommendations)
                .overloadedDays(overloadedDays)
                .summary(summary)
                .build();
    }

    private List<AiTaskRecommendationDto> buildRecommendations(List<Task> tasks) {
        LocalDate today = LocalDate.now();

        return tasks.stream()
                .map(task -> {
                    Integer currentPriority = task.getPriority() != null ? task.getPriority() : 1;
                    Integer recommendedPriority = currentPriority;
                    String reason = "Текущий приоритет выглядит допустимым.";

                    if (task.getDeadline() != null) {
                        long daysUntilDeadline = ChronoUnit.DAYS.between(today, task.getDeadline());

                        if (daysUntilDeadline < 0) {
                            recommendedPriority = 10;
                            reason = "Задача просрочена, рекомендуется максимальный приоритет.";
                        } else if (daysUntilDeadline <= 1) {
                            recommendedPriority = Math.max(currentPriority, 9);
                            reason = "Дедлайн наступает сегодня или завтра.";
                        } else if (daysUntilDeadline <= 3) {
                            recommendedPriority = Math.max(currentPriority, 8);
                            reason = "До дедлайна осталось не более 3 дней.";
                        } else if (daysUntilDeadline <= 7) {
                            recommendedPriority = Math.max(currentPriority, 6);
                            reason = "До дедлайна осталось не более недели.";
                        }
                    }

                    if (task.getStatus() != null
                            && "NEW".equalsIgnoreCase(task.getStatus().getName())
                            && task.getDeadline() != null) {
                        long daysUntilDeadline = ChronoUnit.DAYS.between(today, task.getDeadline());
                        if (daysUntilDeadline >= 0 && daysUntilDeadline <= 3) {
                            recommendedPriority = Math.min(10, recommendedPriority + 1);
                            reason = reason + " Задача ещё не начата.";
                        }
                    }

                    return AiTaskRecommendationDto.builder()
                            .taskId(task.getId().toString())
                            .title(task.getTitle())
                            .currentStatus(task.getStatus() != null ? task.getStatus().getName() : null)
                            .currentPriority(currentPriority)
                            .recommendedPriority(recommendedPriority)
                            .deadline(task.getDeadline() != null ? task.getDeadline().toString() : null)
                            .reason(reason)
                            .build();
                })
                .sorted(Comparator
                        .comparing(AiTaskRecommendationDto::getRecommendedPriority, Comparator.reverseOrder())
                        .thenComparing(dto -> dto.getDeadline() == null ? "9999-12-31" : dto.getDeadline()))
                .collect(Collectors.toList());
    }

    private List<AiOverloadedDayDto> buildOverloadedDays(List<Task> tasks) {
        Map<LocalDate, List<Task>> tasksByDate = tasks.stream()
                .filter(task -> task.getDeadline() != null)
                .collect(Collectors.groupingBy(Task::getDeadline));

        return tasksByDate.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Task> dayTasks = entry.getValue();

                    int taskCount = dayTasks.size();
                    int totalMinutes = dayTasks.stream()
                            .map(task -> task.getEstimatedMinutes() != null ? task.getEstimatedMinutes() : 60)
                            .reduce(0, Integer::sum);

                    String loadLevel = null;

                    if (taskCount >= 5 || totalMinutes > 480) {
                        loadLevel = "HIGH";
                    } else if (taskCount >= 3 || totalMinutes > 240) {
                        loadLevel = "MEDIUM";
                    }

                    if (loadLevel == null) {
                        return null;
                    }

                    return AiOverloadedDayDto.builder()
                            .date(date.toString())
                            .taskCount(taskCount)
                            .totalEstimatedMinutes(totalMinutes)
                            .loadLevel(loadLevel)
                            .build();
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(AiOverloadedDayDto::getDate))
                .collect(Collectors.toList());
    }

    private String buildSummary(
            User user,
            int totalTasks,
            int activeTasks,
            List<AiTaskRecommendationDto> recommendations,
            List<AiOverloadedDayDto> overloadedDays
    ) {
        long criticalTasks = recommendations.stream()
                .filter(r -> r.getRecommendedPriority() != null && r.getRecommendedPriority() >= 8)
                .count();

        return "Для пользователя " + user.getUsername()
                + " проанализировано " + totalTasks + " задач, из них активных: " + activeTasks
                + ". Критически важных задач: " + criticalTasks
                + ". Перегруженных дней обнаружено: " + overloadedDays.size() + ".";
    }

    private UUID parseUuid(String value, String fieldName) {
        try {
            return UUID.fromString(value);
        } catch (Exception ex) {
            throw new BadRequestException("Invalid UUID format for " + fieldName);
        }
    }
}
