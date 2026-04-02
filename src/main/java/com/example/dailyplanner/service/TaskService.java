package com.example.dailyplanner.service;

import com.example.dailyplanner.dto.TaskRequest;
import com.example.dailyplanner.dto.TaskResponse;
import com.example.dailyplanner.enums.SortDirection;
import com.example.dailyplanner.enums.TaskSortField;
import com.example.dailyplanner.exception.BadRequestException;
import com.example.dailyplanner.exception.ResourceNotFoundException;
import com.example.dailyplanner.mapper.TaskMapper;
import com.example.dailyplanner.model.Task;
import com.example.dailyplanner.model.TaskStatus;
import com.example.dailyplanner.model.User;
import com.example.dailyplanner.repository.TaskRepository;
import com.example.dailyplanner.repository.TaskStatusRepository;
import com.example.dailyplanner.repository.UserRepository;
import com.example.dailyplanner.specification.TaskSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskStatusRepository statusRepository;

    public TaskResponse createTask(TaskRequest request) {
        UUID userId = parseUuid(request.getUserId(), "userId");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));

        TaskStatus status = statusRepository.findByName(request.getStatusName())
                .orElseThrow(() -> new ResourceNotFoundException("Status not found: " + request.getStatusName()));

        LocalDate deadline = parseDate(request.getDeadline(), "deadline");

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDeadline(deadline);
        task.setUser(user);
        task.setStatus(status);
        task.setPriority(request.getPriority());

        Task savedTask = taskRepository.save(task);
        return TaskMapper.toResponse(savedTask);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getUserTasks(
            String userId,
            int page,
            int size,
            TaskSortField sortBy,
            SortDirection sortDir
    ) {
        validatePaging(page, size);

        UUID parsedUserId = parseUuid(userId, "userId");

        if (!userRepository.existsById(parsedUserId)) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }

        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Specification<Task> specification = TaskSpecification.hasUserId(parsedUserId);

        return taskRepository.findAll(specification, pageable)
                .map(TaskMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> filterTasks(
            String userId,
            String statusName,
            Integer priority,
            String title,
            String deadlineFrom,
            String deadlineTo,
            int page,
            int size,
            TaskSortField sortBy,
            SortDirection sortDir
    ) {
        validatePaging(page, size);

        UUID parsedUserId = userId == null || userId.isBlank() ? null : parseUuid(userId, "userId");
        LocalDate parsedDeadlineFrom = parseDate(deadlineFrom, "deadlineFrom");
        LocalDate parsedDeadlineTo = parseDate(deadlineTo, "deadlineTo");

        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Specification<Task> specification = Specification
                .where(TaskSpecification.hasUserId(parsedUserId))
                .and(TaskSpecification.hasStatusName(statusName))
                .and(TaskSpecification.hasPriority(priority))
                .and(TaskSpecification.titleContains(title))
                .and(TaskSpecification.deadlineFrom(parsedDeadlineFrom))
                .and(TaskSpecification.deadlineTo(parsedDeadlineTo));

        return taskRepository.findAll(specification, pageable)
                .map(TaskMapper::toResponse);
    }

    public TaskResponse updateTaskTitle(String taskId, String title) {
        if (title == null || title.isBlank()) {
            throw new BadRequestException("Title must not be blank");
        }

        UUID parsedTaskId = parseUuid(taskId, "taskId");

        Task task = taskRepository.findById(parsedTaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        task.setTitle(title);

        return TaskMapper.toResponse(taskRepository.save(task));
    }

    public void deleteTask(String taskId) {
        UUID parsedTaskId = parseUuid(taskId, "taskId");

        if (!taskRepository.existsById(parsedTaskId)) {
            throw new ResourceNotFoundException("Task not found: " + taskId);
        }

        taskRepository.deleteById(parsedTaskId);
    }

    public TaskResponse changeStatus(String taskId, String statusName) {
        if (statusName == null || statusName.isBlank()) {
            throw new BadRequestException("Status name must not be blank");
        }

        UUID parsedTaskId = parseUuid(taskId, "taskId");

        Task task = taskRepository.findById(parsedTaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        TaskStatus status = statusRepository.findByName(statusName)
                .orElseThrow(() -> new ResourceNotFoundException("Status not found: " + statusName));

        task.setStatus(status);

        return TaskMapper.toResponse(taskRepository.save(task));
    }

    private Pageable buildPageable(int page, int size, TaskSortField sortBy, SortDirection sortDir) {
        Sort sort = sortDir == SortDirection.DESC
                ? Sort.by(sortBy.getFieldName()).descending()
                : Sort.by(sortBy.getFieldName()).ascending();

        return PageRequest.of(page, size, sort);
    }

    private void validatePaging(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("Page must be greater than or equal to 0");
        }

        if (size <= 0) {
            throw new BadRequestException("Size must be greater than 0");
        }
    }

    private LocalDate parseDate(String date, String fieldName) {
        if (date == null || date.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(date);
        } catch (Exception ex) {
            throw new BadRequestException("Invalid date format for " + fieldName + ". Expected format: yyyy-MM-dd");
        }
    }

    private UUID parseUuid(String value, String fieldName) {
        try {
            return UUID.fromString(value);
        } catch (Exception ex) {
            throw new BadRequestException("Invalid UUID format for " + fieldName);
        }
    }
}
