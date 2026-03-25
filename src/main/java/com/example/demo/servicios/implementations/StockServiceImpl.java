package com.example.demo.servicios.implementations;

import com.example.demo.entidades.Active;
import com.example.demo.repositorios.ActiveRepository;
import com.example.demo.repositorios.PriceDataRepository;
import com.example.demo.servicios.interfaces.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class StockServiceImpl implements StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockServiceImpl.class);
    private static final int MIN_DELAY_BETWEEN_REQUESTS = 5000;

    private final ActiveRepository activeRepository;
    private final PriceDataRepository priceDataRepository;
    private final StockFetchService stockFetchService;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicInteger progress = new AtomicInteger(0);
    private final Set<String> processedSymbols = ConcurrentHashMap.newKeySet();
    private final Path stateFile = Paths.get("logs/job_state.txt");

    public StockServiceImpl(ActiveRepository activeRepository, PriceDataRepository priceDataRepository, StockFetchService stockFetchService) {
        this.activeRepository = activeRepository;
        this.priceDataRepository = priceDataRepository;
        this.stockFetchService = stockFetchService;
        loadState();
    }

    private void loadState() {
        try {
            if (Files.exists(stateFile)) {
                List<String> lines = Files.readAllLines(stateFile);
                for (String line : lines) {
                    if (line.startsWith("processed:")) {
                        String[] symbols = line.substring(9).split(",");
                        for (String s : symbols) {
                            if (!s.trim().isEmpty()) {
                                processedSymbols.add(s.trim());
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("Could not load state file: {}", e.getMessage());
        }
    }

    private void saveState() {
        try {
            Files.createDirectories(stateFile.getParent());
            String symbols = String.join(",", processedSymbols);
            String content = "processed:" + symbols;
            Files.writeString(stateFile, content);
        } catch (IOException e) {
            logger.warn("Could not save state file: {}", e.getMessage());
        }
    }

    public boolean isJobRunning() {
        return isRunning.get();
    }

    public int getProgress() {
        return progress.get();
    }

    public int getRemainingRequests() {
        return Integer.MAX_VALUE;
    }

    public Set<String> getProcessedSymbols() {
        return Set.copyOf(processedSymbols);
    }

    @Override
    public List<String> fetchHistoricalData(String symbol, LocalDate startDate) {
        return stockFetchService.fetchHistoricalData(symbol, startDate);
    }

    @Async
    public void fetchAllActivesData() {
        if (!isRunning.compareAndSet(false, true)) {
            logger.warn("Job already running, skipping request");
            return;
        }

        List<Active> allActives = activeRepository.findAll();
        List<String> allErrors = new ArrayList<>();

        try {
            logger.info("Starting historical data fetch from Yahoo Finance");

            List<Active> activesToProcess = allActives.stream()
                    .filter(a -> !processedSymbols.contains(a.getSymbol()))
                    .collect(Collectors.toList());

            if (activesToProcess.isEmpty()) {
                logger.info("All actives have been processed. Reset processed list to fetch again.");
                processedSymbols.clear();
                activesToProcess = allActives;
            }

            int total = activesToProcess.size();
            int current = 0;

            for (Active active : activesToProcess) {
                current++;
                progress.set((current * 100) / total);
                logger.info("Processing active {}/{}: {}", current, total, active.getSymbol());
                List<String> errors = stockFetchService.fetchHistoricalData(active.getSymbol(), LocalDate.of(2019, 1, 1));
                allErrors.addAll(errors);
                processedSymbols.add(active.getSymbol());
                saveState();

                if (current < total) {
                    logger.info("Waiting {}ms before next request...", MIN_DELAY_BETWEEN_REQUESTS);
                    try {
                        Thread.sleep(MIN_DELAY_BETWEEN_REQUESTS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            progress.set(100);
            saveState();
            logger.info("Job completed. Processed {} symbols. Errors: {}", processedSymbols.size(), allErrors);

        } finally {
            isRunning.set(false);
        }
    }

    public void resetProcessedSymbols() {
        processedSymbols.clear();
        saveState();
        logger.info("Processed symbols reset");
    }
}
