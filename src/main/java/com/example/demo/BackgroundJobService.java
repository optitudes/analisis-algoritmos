package com.example.demo;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

@Service
public class BackgroundJobService {

    private static final String LOG_FILE = "background_job.log";

    @Async
    public void executeBackgroundJob() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, false))) {
            for (int i = 1; i <= 100; i++) {
                writer.println(i);
                writer.flush();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
