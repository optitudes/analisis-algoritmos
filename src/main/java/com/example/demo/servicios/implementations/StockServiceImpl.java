package com.example.demo.servicios.implementations;

import com.example.demo.entidades.Active;
import com.example.demo.entidades.PriceData;
import com.example.demo.repositorios.ActiveRepository;
import com.example.demo.repositorios.PriceDataRepository;
import com.example.demo.servicios.interfaces.StockService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class StockServiceImpl implements StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockServiceImpl.class);
    private static final String[] YAHOO_HOSTNAMES = {
        "query1.finance.yahoo.com",
        "query2.finance.yahoo.com"
    };
    private static final String YAHOO_FINANCE_URL = "https://%s/v8/finance/chart/%s?period1=%d&period2=%d&interval=1d";
    private static final int MAX_RETRIES = 5;
    private static final int MIN_DELAY_BETWEEN_REQUESTS = 5000;
    private static final int RATE_LIMIT_DELAY = 30000;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ActiveRepository activeRepository;
    private final PriceDataRepository priceDataRepository;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicInteger progress = new AtomicInteger(0);
    private final Set<String> processedSymbols = ConcurrentHashMap.newKeySet();
    private final Path stateFile = Paths.get("logs/job_state.txt");

    public StockServiceImpl(ActiveRepository activeRepository, PriceDataRepository priceDataRepository) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.activeRepository = activeRepository;
        this.priceDataRepository = priceDataRepository;
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
    @Async
    @Transactional
    public List<String> fetchHistoricalData(String symbol, LocalDate startDate) {
        List<String> errors = new ArrayList<>();
        LocalDate targetDate = startDate != null ? startDate : LocalDate.of(2019, 1, 1);
        LocalDate today = LocalDate.now();
        LocalDate chunkEnd = today;
        final int CHUNK_DAYS = 365;

        try {
            Optional<Active> activeOpt = activeRepository.findBySymbol(symbol);
            if (activeOpt.isEmpty()) {
                errors.add("Active not found for symbol: " + symbol);
                return errors;
            }
            Active active = activeOpt.get();

            Set<LocalDate> existingDates = priceDataRepository.findByActive(active)
                    .stream()
                    .map(PriceData::getDate)
                    .collect(Collectors.toSet());

            int totalRecordsSaved = 0;

            while (chunkEnd.isAfter(targetDate)) {
                LocalDate chunkStart = chunkEnd.minusDays(CHUNK_DAYS);
                if (chunkStart.isBefore(targetDate)) {
                    chunkStart = targetDate;
                }

                long startTimestamp = chunkStart.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
                long endTimestamp = chunkEnd.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

                String hostname = YAHOO_HOSTNAMES[(symbol.hashCode() & Integer.MAX_VALUE) % YAHOO_HOSTNAMES.length];
                String apiUrl = String.format(YAHOO_FINANCE_URL, hostname, URLEncoder.encode(symbol, java.nio.charset.StandardCharsets.UTF_8), startTimestamp, endTimestamp);
                
                logger.info("Fetching chunk for {} from {} to {}", symbol, chunkStart, chunkEnd);

                String response = fetchWithRetry(apiUrl, symbol);
                if (response == null || response.isEmpty()) {
                    errors.add("Empty response from API for " + symbol + " (" + chunkStart + " to " + chunkEnd + ")");
                    chunkEnd = chunkStart.minusDays(1);
                    continue;
                }

                try {
                    JsonNode root = objectMapper.readTree(response);
                    JsonNode chart = root.path("chart");
                    JsonNode result = chart.path("result");

                    if (result.isMissingNode() || result.isEmpty()) {
                        String error = chart.path("error").path("description").asText();
                        if (!error.isEmpty() && !error.contains("No data found")) {
                            errors.add("Yahoo Finance error for " + symbol + ": " + error);
                        }
                        chunkEnd = chunkStart.minusDays(1);
                        continue;
                    }

                    JsonNode chartResult = result.get(0);
                    JsonNode timestamps = chartResult.path("timestamp");
                    JsonNode quote = chartResult.path("indicators").path("quote").get(0);
                    JsonNode adjclose = chartResult.path("indicators").path("adjclose");

                    if (timestamps.isMissingNode() || quote.isMissingNode() || timestamps.size() == 0) {
                        chunkEnd = chunkStart.minusDays(1);
                        continue;
                    }

                    boolean useAdjclose = !adjclose.isMissingNode() && adjclose.isArray() && adjclose.size() > 0;
                    
                    JsonNode openNode = quote.path("open");
                    JsonNode highNode = quote.path("high");
                    JsonNode lowNode = quote.path("low");
                    JsonNode closeNode = quote.path("close");
                    JsonNode volumeNode = quote.path("volume");
                    JsonNode adjcloseNode = useAdjclose ? adjclose.get(0) : null;

                    List<PriceData> dataToSave = new ArrayList<>();
                    int dataSize = timestamps.size();

                    for (int i = 0; i < dataSize; i++) {
                        try {
                            if (i >= openNode.size() || i >= highNode.size() || i >= lowNode.size() || 
                                i >= closeNode.size() || i >= volumeNode.size()) {
                                continue;
                            }
                            
                            if (adjcloseNode != null && i >= adjcloseNode.size()) {
                                continue;
                            }

                            long timestamp = timestamps.get(i).asLong();
                            LocalDate date = Instant.ofEpochSecond(timestamp)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate();

                            if (existingDates.contains(date)) {
                                continue;
                            }

                            JsonNode openNode_i = openNode.get(i);
                            JsonNode highNode_i = highNode.get(i);
                            JsonNode lowNode_i = lowNode.get(i);
                            JsonNode closeNode_i = closeNode.get(i);
                            JsonNode volumeNode_i = volumeNode.get(i);
                            
                            if (openNode_i == null || highNode_i == null || lowNode_i == null || 
                                closeNode_i == null || volumeNode_i == null) {
                                continue;
                            }

                            double open = openNode_i.isNull() ? 0.0 : openNode_i.asDouble(0);
                            double high = highNode_i.isNull() ? 0.0 : highNode_i.asDouble(0);
                            double low = lowNode_i.isNull() ? 0.0 : lowNode_i.asDouble(0);
                            double close = closeNode_i.isNull() ? 0.0 : closeNode_i.asDouble(0);
                            double adjClose = close;
                            
                            if (useAdjclose && adjcloseNode != null && i < adjcloseNode.size()) {
                                JsonNode adjcloseNode_i = adjcloseNode.get(i);
                                if (adjcloseNode_i != null && !adjcloseNode_i.isNull()) {
                                    adjClose = adjcloseNode_i.asDouble(close);
                                }
                            } else if (!useAdjclose) {
                                adjClose = close;
                            }
                            
                            long volume = volumeNode_i.isNull() ? 0L : volumeNode_i.asLong(0);

                            if (open == 0 && high == 0 && low == 0 && close == 0) {
                                continue;
                            }

                            PriceData priceData = new PriceData();
                            priceData.setDate(date);
                            priceData.setOpen(open);
                            priceData.setHigh(high);
                            priceData.setLow(low);
                            priceData.setClose(adjClose);
                            priceData.setVolume(volume);
                            priceData.setActive(active);

                            dataToSave.add(priceData);
                            existingDates.add(date);
                        } catch (Exception e) {
                            logger.warn("Error parsing data for {} at index {}: {}", symbol, i, e.getMessage());
                        }
                    }

                    if (!dataToSave.isEmpty()) {
                        priceDataRepository.saveAll(dataToSave);
                        totalRecordsSaved += dataToSave.size();
                        logger.info("Saved {} records for {} in chunk {} to {}", dataToSave.size(), symbol, chunkStart, chunkEnd);
                    }

                    chunkEnd = chunkStart.minusDays(1);
                    
                } catch (Exception e) {
                    logger.error("Error parsing response for {}: {}", symbol, e.getMessage());
                    errors.add("Parse error for " + symbol + ": " + e.getMessage());
                    chunkEnd = chunkStart.minusDays(1);
                }
            }

            if (totalRecordsSaved > 0) {
                logger.info("Total saved {} records for {}", totalRecordsSaved, symbol);
            } else if (errors.isEmpty()) {
                logger.info("No new records to save for {}", symbol);
            }

            processedSymbols.add(symbol);
            saveState();

        } catch (Exception e) {
            logger.error("Error fetching data for " + symbol + ": " + e.getMessage());
            errors.add(symbol + ": " + e.getMessage());
        }

        return errors;
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
                List<String> errors = fetchHistoricalData(active.getSymbol(), LocalDate.of(2019, 1, 1));
                allErrors.addAll(errors);

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

    private String fetchWithRetry(String apiUrl, String symbol) {
        int attempts = 0;
        long waitTime = 5000;
        boolean rateLimited = false;

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.set("Accept", "application/json");
        headers.set("Accept-Language", "en-US,en;q=0.9");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        while (attempts < MAX_RETRIES) {
            attempts++;
            try {
                ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, String.class);
                return response.getBody();
            } catch (Exception e) {
                String errorMsg = e.getMessage();
                if (errorMsg.contains("429")) {
                    rateLimited = true;
                    logger.warn("Rate limited for {}, waiting {}ms (attempt {}/{})", symbol, waitTime, attempts, MAX_RETRIES);
                } else {
                    logger.warn("Attempt {}/{} failed for {}: {}", attempts, MAX_RETRIES, symbol, errorMsg);
                }
                
                if (attempts < MAX_RETRIES) {
                    try {
                        Thread.sleep(waitTime);
                        waitTime *= 2;
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
            }
        }
        
        if (rateLimited) {
            logger.error("Rate limit reached for {}. Consider waiting 60 seconds before retrying.", symbol);
            try {
                Thread.sleep(RATE_LIMIT_DELAY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }

    public void resetProcessedSymbols() {
        processedSymbols.clear();
        saveState();
        logger.info("Processed symbols reset");
    }
}
