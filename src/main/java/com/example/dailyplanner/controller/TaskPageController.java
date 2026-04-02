package com.example.dailyplanner.controller;

import com.example.dailyplanner.dto.TaskRequest;
import com.example.dailyplanner.dto.TaskResponse;
import com.example.dailyplanner.enums.SortDirection;
import com.example.dailyplanner.enums.TaskSortField;
import com.example.dailyplanner.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tasks-ui")
@RequiredArgsConstructor
public class TaskPageController {

    private static final String DEMO_USER_ID = "b3149c21-a07b-450f-9deb-2166ad1d6a68";

    private final TaskService taskService;

    @GetMapping
    public String getTasksPage(
            @RequestParam(required = false) String statusName,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String deadlineFrom,
            @RequestParam(required = false) String deadlineTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DEADLINE") TaskSortField sortBy,
            @RequestParam(defaultValue = "ASC") SortDirection sortDir,
            @RequestParam(required = false) String message,
            Model model
    ) {
        Page<TaskResponse> taskPage = taskService.filterTasks(
                DEMO_USER_ID,
                statusName,
                priority,
                title,
                deadlineFrom,
                deadlineTo,
                page,
                size,
                sortBy,
                sortDir
        );

        model.addAttribute("tasks", taskPage.getContent());
        model.addAttribute("taskPage", taskPage);
        model.addAttribute("userId", DEMO_USER_ID);
        model.addAttribute("statusName", statusName);
        model.addAttribute("priority", priority);
        model.addAttribute("title", title);
        model.addAttribute("deadlineFrom", deadlineFrom);
        model.addAttribute("deadlineTo", deadlineTo);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("message", message);

        return "tasks";
    }

    @GetMapping("/create")
    public String getCreateTaskPage(Model model) {
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setUserId(DEMO_USER_ID);
        taskRequest.setStatusName("NEW");

        model.addAttribute("taskRequest", taskRequest);
        model.addAttribute("userId", DEMO_USER_ID);

        return "task-create";
    }

    @PostMapping("/create")
    public String createTask(@ModelAttribute TaskRequest taskRequest) {
        taskRequest.setUserId(DEMO_USER_ID);
        taskService.createTask(taskRequest);
        return "redirect:/tasks-ui?message=created";
    }

    @PostMapping("/{taskId}/status")
    public String changeTaskStatus(
            @PathVariable String taskId,
            @RequestParam String status,
            @RequestParam(required = false) String statusName,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String deadlineFrom,
            @RequestParam(required = false) String deadlineTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DEADLINE") TaskSortField sortBy,
            @RequestParam(defaultValue = "ASC") SortDirection sortDir
    ) {
        taskService.changeStatus(taskId, status);
        return buildRedirectUrl(statusName, priority, title, deadlineFrom, deadlineTo, page, size, sortBy, sortDir, "status-updated");
    }

    @PostMapping("/{taskId}/delete")
    public String deleteTask(
            @PathVariable String taskId,
            @RequestParam(required = false) String statusName,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String deadlineFrom,
            @RequestParam(required = false) String deadlineTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DEADLINE") TaskSortField sortBy,
            @RequestParam(defaultValue = "ASC") SortDirection sortDir
    ) {
        taskService.deleteTask(taskId);
        return buildRedirectUrl(statusName, priority, title, deadlineFrom, deadlineTo, page, size, sortBy, sortDir, "deleted");
    }

    private String buildRedirectUrl(
            String statusName,
            Integer priority,
            String title,
            String deadlineFrom,
            String deadlineTo,
            int page,
            int size,
            TaskSortField sortBy,
            SortDirection sortDir,
            String message
    ) {
        StringBuilder redirectUrl = new StringBuilder("redirect:/tasks-ui");

        boolean hasQuery = false;

        if (message != null && !message.isBlank()) {
            redirectUrl.append("?message=").append(message);
            hasQuery = true;
        }

        if (statusName != null && !statusName.isBlank()) {
            redirectUrl.append(hasQuery ? "&" : "?").append("statusName=").append(statusName);
            hasQuery = true;
        }
        if (priority != null) {
            redirectUrl.append(hasQuery ? "&" : "?").append("priority=").append(priority);
            hasQuery = true;
        }
        if (title != null && !title.isBlank()) {
            redirectUrl.append(hasQuery ? "&" : "?").append("title=").append(title);
            hasQuery = true;
        }
        if (deadlineFrom != null && !deadlineFrom.isBlank()) {
            redirectUrl.append(hasQuery ? "&" : "?").append("deadlineFrom=").append(deadlineFrom);
            hasQuery = true;
        }
        if (deadlineTo != null && !deadlineTo.isBlank()) {
            redirectUrl.append(hasQuery ? "&" : "?").append("deadlineTo=").append(deadlineTo);
            hasQuery = true;
        }

        redirectUrl.append(hasQuery ? "&" : "?").append("page=").append(page);
        redirectUrl.append("&size=").append(size);
        redirectUrl.append("&sortBy=").append(sortBy);
        redirectUrl.append("&sortDir=").append(sortDir);

        return redirectUrl.toString();
    }
}
