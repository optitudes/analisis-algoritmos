package com.example.demo;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JobController {

    private final BackgroundJobService backgroundJobService;

    public JobController(BackgroundJobService backgroundJobService) {
        this.backgroundJobService = backgroundJobService;
    }

    @PostMapping("/execute-job")
    public void executeJob() {
        backgroundJobService.executeBackgroundJob();
    }
}
