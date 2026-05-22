package com.example.dailyplanner.controller;

import com.example.dailyplanner.dto.AiAnalysisResponse;
import com.example.dailyplanner.service.TaskAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai-analysis")
@RequiredArgsConstructor
public class TaskAiController {

    private final TaskAiService taskAiService;

    @GetMapping("/{userId}")
    public AiAnalysisResponse analyzeUserTasks(@PathVariable String userId) {
        return taskAiService.analyzeUserTasks(userId);
    }
}
