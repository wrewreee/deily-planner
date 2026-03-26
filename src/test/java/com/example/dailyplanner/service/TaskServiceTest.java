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

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("user-1");
        user.setUsername("test_user");
        user.setEmail("test@example.com");
        user.setPassword("password");

        status = new TaskStatus();
        status.setId("status-1");
        status.setName("NEW");

        task = new Task();
        task.setId("task-1");
        task.setTitle("Подготовить диплом");
        task.setDeadline(LocalDate.of(2026, 3, 20));
        task.setPriority(5);
        task.setUser(user);
        task.setStatus(status);

        taskRequest = new TaskRequest();
        taskRequest.setTitle("Подготовить диплом");
        taskRequest.setUserId("user-1");
        taskRequest.setStatusName("NEW");
        taskRequest.setDeadline("2026-03-20");
        taskRequest.setPriority(5);
    }

    @Test
    void createTask_success() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(statusRepository.findByName("NEW")).thenReturn(Optional.of(status));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse response = taskService.createTask(taskRequest);

        assertNotNull(response);
        assertEquals("task-1", response.getId());
        assertEquals("Подготовить диплом", response.getTitle());
        assertEquals("NEW", response.getStatus());
        assertEquals("user-1", response.getUserId());
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
        when(userRepository.findById("user-1")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.createTask(taskRequest)
        );

        assertEquals("User not found: user-1", exception.getMessage());

        verify(taskRepository, never()).save(any(Task.class));
        verify(statusRepository, never()).findByName(anyString());
    }

    @Test
    void updateTaskTitle_blankTitle() {
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> taskService.updateTaskTitle("task-1", "   ")
        );

        assertEquals("Title must not be blank", exception.getMessage());

        verify(taskRepository, never()).findById(anyString());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void changeStatus_success() {
        TaskStatus newStatus = new TaskStatus();
        newStatus.setId("status-2");
        newStatus.setName("IN_PROGRESS");

        when(taskRepository.findById("task-1")).thenReturn(Optional.of(task));
        when(statusRepository.findByName("IN_PROGRESS")).thenReturn(Optional.of(newStatus));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.changeStatus("task-1", "IN_PROGRESS");

        assertNotNull(response);
        assertEquals("task-1", response.getId());
        assertEquals("IN_PROGRESS", response.getStatus());
        assertEquals("Подготовить диплом", response.getTitle());

        assertEquals("IN_PROGRESS", task.getStatus().getName());

        verify(taskRepository).findById("task-1");
        verify(statusRepository).findByName("IN_PROGRESS");
        verify(taskRepository).save(task);
    }
}
