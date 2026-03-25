package com.example.demo.controllers;

import com.example.demo.BackgroundJobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final BackgroundJobService backgroundJobService;

    public JobController(BackgroundJobService backgroundJobService) {
        this.backgroundJobService = backgroundJobService;
    }

    @PostMapping("/fetch-historical-data")
    public ResponseEntity<Map<String, Object>> executeJob() {
        if (backgroundJobService.isJobRunning()) {
            return ResponseEntity.ok(Map.of(
                "status", "already_running",
                "progress", backgroundJobService.getProgress()
            ));
        }

        backgroundJobService.executeBackgroundJob();

        return ResponseEntity.accepted().body(Map.of(
            "status", "started",
            "message", "Historical data fetch job started in background (Yahoo Finance)"
        ));
    }

    @PostMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
            "running", backgroundJobService.isJobRunning(),
            "progress", backgroundJobService.getProgress(),
            "processedSymbols", backgroundJobService.getProcessedSymbols()
        ));
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetJob() {
        backgroundJobService.resetProcessedSymbols();
        return ResponseEntity.ok(Map.of(
            "status", "reset",
            "message", "Processed symbols have been reset"
        ));
    }
}
