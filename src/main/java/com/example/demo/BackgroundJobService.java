package com.example.demo;

import com.example.demo.servicios.implementations.StockServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class BackgroundJobService {

    private final StockServiceImpl stockService;

    public BackgroundJobService(StockServiceImpl stockService) {
        this.stockService = stockService;
    }

    @Async
    public void executeBackgroundJob() {
        stockService.fetchAllActivesData();
    }

    public boolean isJobRunning() {
        return stockService.isJobRunning();
    }

    public int getProgress() {
        return stockService.getProgress();
    }

    public Set<String> getProcessedSymbols() {
        return stockService.getProcessedSymbols();
    }

    public void resetProcessedSymbols() {
        stockService.resetProcessedSymbols();
    }
}
