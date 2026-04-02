package com.example.dailyplanner.service;

import com.example.dailyplanner.dto.TaskRequest;
import com.example.dailyplanner.dto.TaskResponse;
import com.example.dailyplanner.exception.BadRequestException;
import com.example.dailyplanner.exception.ResourceNotFoundException;
import com.example.dailyplanner.model.Task;
import com.example.dailyplanner.model.TaskStatus;
import com.example.dailyplanner.model.User;
import com.example.dailyplanner.repository.TaskRepository;
import com.example.dailyplanner.repository.TaskStatusRepository;
import com.example.dailyplanner.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskStatusRepository statusRepository;

    @InjectMocks
    private TaskService taskService;

    private User user;
    private TaskStatus status;
    private Task task;
    private TaskRequest taskRequest;

    private final UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID taskId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final UUID statusId = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private final UUID newStatusId = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(userId);
        user.setUsername("test_user");
        user.setEmail("test@example.com");
        user.setPassword("password");

        status = new TaskStatus();
        status.setId(statusId);
        status.setName("NEW");

        task = new Task();
        task.setId(taskId);
        task.setTitle("Подготовить диплом");
        task.setDeadline(LocalDate.of(2026, 3, 20));
        task.setPriority(5);
        task.setUser(user);
        task.setStatus(status);

        taskRequest = new TaskRequest();
        taskRequest.setTitle("Подготовить диплом");
        taskRequest.setUserId(userId.toString());
        taskRequest.setStatusName("NEW");
        taskRequest.setDeadline("2026-03-20");
        taskRequest.setPriority(5);
    }

    @Test
    void createTask_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(statusRepository.findByName("NEW")).thenReturn(Optional.of(status));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse response = taskService.createTask(taskRequest);

        assertNotNull(response);
        assertEquals(taskId.toString(), response.getId());
        assertEquals("Подготовить диплом", response.getTitle());
        assertEquals("NEW", response.getStatus());
        assertEquals(userId.toString(), response.getUserId());
        assertEquals(5, response.getPriority());

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());

        Task savedTask = taskCaptor.getValue();
        assertEquals("Подготовить диплом", savedTask.getTitle());
        assertEquals(LocalDate.of(2026, 3, 20), savedTask.getDeadline());
        assertEquals(5, savedTask.getPriority());
        assertEquals(user, savedTask.getUser());
        assertEquals(status, savedTask.getStatus());
    }

    @Test
    void createTask_userNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.createTask(taskRequest)
        );

        assertEquals("User not found: " + userId, exception.getMessage());

        verify(taskRepository, never()).save(any(Task.class));
        verify(statusRepository, never()).findByName(anyString());
    }

    @Test
    void updateTaskTitle_blankTitle() {
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> taskService.updateTaskTitle(taskId.toString(), "   ")
        );

        assertEquals("Title must not be blank", exception.getMessage());

        verify(taskRepository, never()).findById(any());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void changeStatus_success() {
        TaskStatus newStatus = new TaskStatus();
        newStatus.setId(newStatusId);
        newStatus.setName("IN_PROGRESS");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(statusRepository.findByName("IN_PROGRESS")).thenReturn(Optional.of(newStatus));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.changeStatus(taskId.toString(), "IN_PROGRESS");

        assertNotNull(response);
        assertEquals(taskId.toString(), response.getId());
        assertEquals("IN_PROGRESS", response.getStatus());
        assertEquals("Подготовить диплом", response.getTitle());

        assertEquals("IN_PROGRESS", task.getStatus().getName());

        verify(taskRepository).findById(taskId);
        verify(statusRepository).findByName("IN_PROGRESS");
        verify(taskRepository).save(task);
    }
}
