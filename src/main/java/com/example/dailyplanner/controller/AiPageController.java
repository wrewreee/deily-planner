package com.example.dailyplanner.controller;

import com.example.dailyplanner.dto.AiAnalysisResponse;
import com.example.dailyplanner.service.TaskAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AiPageController {

    private static final String DEMO_USER_ID = "b3149c21-a07b-450f-9deb-2166ad1d6a68";

    private final TaskAiService taskAiService;

    @GetMapping("/ai-dashboard")
    public String getAiDashboard(Model model) {
        AiAnalysisResponse analysis = taskAiService.analyzeUserTasks(DEMO_USER_ID);
        model.addAttribute("analysis", analysis);
        return "ai-dashboard";
    }
}
