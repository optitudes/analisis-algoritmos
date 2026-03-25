package com.example.demo.servicios.implementations;

import com.example.demo.entidades.Active;
import com.example.demo.entidades.PriceData;
import com.example.demo.repositorios.ActiveRepository;
import com.example.demo.repositorios.PriceDataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StockFetchService {

    private static final Logger logger = LoggerFactory.getLogger(StockFetchService.class);
    private static final String[] YAHOO_HOSTNAMES = {
        "query1.finance.yahoo.com",
        "query2.finance.yahoo.com"
    };
    private static final String YAHOO_FINANCE_URL = "https://%s/v8/finance/chart/%s?period1=%d&period2=%d&interval=1d";
    private static final int MAX_RETRIES = 5;
    private static final int RATE_LIMIT_DELAY = 30000;
    private static final ZoneId NY_ZONE = ZoneId.of("America/New_York");

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ActiveRepository activeRepository;
    private final PriceDataRepository priceDataRepository;

    public StockFetchService(ActiveRepository activeRepository, PriceDataRepository priceDataRepository) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.activeRepository = activeRepository;
        this.priceDataRepository = priceDataRepository;
    }

    @Transactional
    public List<String> fetchHistoricalData(String symbol, LocalDate startDate) {
        List<String> errors = new ArrayList<>();

        try {
            Optional<Active> activeOpt = activeRepository.findBySymbol(symbol);
            if (activeOpt.isEmpty()) {
                errors.add("Active not found for symbol: " + symbol);
                return errors;
            }
            Active active = activeOpt.get();

            priceDataRepository.deleteByActive(active);
            priceDataRepository.flush();

            LocalDate start = LocalDate.of(2019, 1, 1);
            LocalDate end = LocalDate.of(2025, 1, 1);
            long startTimestamp = start.atStartOfDay(NY_ZONE).toEpochSecond();
            long endTimestamp = end.atStartOfDay(NY_ZONE).toEpochSecond();

            String hostname = YAHOO_HOSTNAMES[(symbol.hashCode() & Integer.MAX_VALUE) % YAHOO_HOSTNAMES.length];
            String apiUrl = String.format(YAHOO_FINANCE_URL, hostname, URLEncoder.encode(symbol, java.nio.charset.StandardCharsets.UTF_8), startTimestamp, endTimestamp);

            logger.info("Fetching data for {} from {} to {}", symbol, start, end);

            String response = fetchWithRetry(apiUrl, symbol);
            if (response == null || response.isEmpty()) {
                errors.add("Empty response from API for " + symbol);
                return errors;
            }

            logger.info("Response received for {}, response length: {}", symbol, response.length());
            logger.info("Full response for {}: {}", symbol, response);

            JsonNode root = objectMapper.readTree(response);
            JsonNode chart = root.path("chart");
            JsonNode result = chart.path("result");

            if (result.isMissingNode() || result.isEmpty()) {
                String error = chart.path("error").path("description").asText();
                logger.warn("No result for {}: error={}", symbol, error);
                if (!error.isEmpty() && !error.contains("No data found")) {
                    errors.add("Yahoo Finance error for " + symbol + ": " + error);
                }
                return errors;
            }

            JsonNode chartResult = result.get(0);
            JsonNode timestamps = chartResult.path("timestamp");
            JsonNode quote = chartResult.path("indicators").path("quote").get(0);
            JsonNode adjclose = chartResult.path("indicators").path("adjclose");

            logger.info("For {}: timestamps={}, quote={}, adjclose={}", symbol, 
                timestamps.isMissingNode() ? "missing" : timestamps.size(),
                quote.isMissingNode() ? "missing" : "present",
                adjclose.isMissingNode() ? "missing" : "present");

            if (timestamps.isMissingNode() || quote.isMissingNode() || timestamps.size() == 0) {
                errors.add("Symbol not found or no data in Yahoo Finance: " + symbol);
                logger.error("No timestamps found for symbol: {}. Possible invalid symbol or no data available.", symbol);
                return errors;
            }

            boolean useAdjclose = !adjclose.isMissingNode() && adjclose.isArray() && adjclose.size() > 0;

            JsonNode openNode = quote.path("open");
            JsonNode highNode = quote.path("high");
            JsonNode lowNode = quote.path("low");
            JsonNode closeNode = quote.path("close");
            JsonNode volumeNode = quote.path("volume");
            JsonNode adjcloseNode = useAdjclose ? adjclose.get(0).path("adjclose") : null;

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
                    LocalDate date = convertTimestampToDate(timestamp);

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

                    logger.info("[ASR] {} | open={} high={} low={} close={} volume={}", date, open, high, low, adjClose, volume);

                    PriceData priceData = new PriceData();
                    priceData.setDate(date);
                    priceData.setOpen(open);
                    priceData.setHigh(high);
                    priceData.setLow(low);
                    priceData.setClose(adjClose);
                    priceData.setVolume(volume);
                    priceData.setActive(active);

                    dataToSave.add(priceData);
                } catch (Exception e) {
                    logger.warn("Error parsing data for {} at index {}: {}", symbol, i, e.getMessage());
                }
            }

            if (!dataToSave.isEmpty()) {
                priceDataRepository.saveAll(dataToSave);
                priceDataRepository.flush();
                logger.info("Saved {} records for {}", dataToSave.size(), symbol);
            } else {
                logger.info("No records to save for {}", symbol);
            }

        } catch (Exception e) {
            logger.error("Error fetching data for " + symbol + ": " + e.getMessage());
            errors.add(symbol + ": " + e.getMessage());
        }

        return errors;
    }

    private LocalDate convertTimestampToDate(long timestamp) {
        long normalizedTimestamp = timestamp;
        if (timestamp > 1_000_000_000_000L) {
            normalizedTimestamp = timestamp / 1000;
        }
        return Instant.ofEpochSecond(normalizedTimestamp)
                .atZone(NY_ZONE)
                .toLocalDate();
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
}
