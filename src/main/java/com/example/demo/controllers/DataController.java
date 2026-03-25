package com.example.demo.controllers;

import com.example.demo.entidades.Active;
import com.example.demo.entidades.PriceData;
import com.example.demo.repositorios.ActiveRepository;
import com.example.demo.repositorios.PriceDataRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DataController {

    private final ActiveRepository activeRepository;
    private final PriceDataRepository priceDataRepository;

    public DataController(ActiveRepository activeRepository, PriceDataRepository priceDataRepository) {
        this.activeRepository = activeRepository;
        this.priceDataRepository = priceDataRepository;
    }

    @GetMapping("/actives")
    public ResponseEntity<Map<String, Object>> getAllActives() {
        List<Active> actives = activeRepository.findAll();
        
        List<Map<String, Object>> activesData = actives.stream().map(active -> {
            long priceCount = priceDataRepository.findByActive(active).size();
            return Map.<String, Object>of(
                "id", active.getId(),
                "name", active.getName(),
                "symbol", active.getSymbol(),
                "priceCount", priceCount
            );
        }).toList();

        return ResponseEntity.ok(Map.of(
            "actives", activesData,
            "count", activesData.size()
        ));
    }

    @GetMapping("/actives/{id}/prices")
    public ResponseEntity<Map<String, Object>> getPricesByActive(@PathVariable Long id) {
        return activeRepository.findById(id)
            .map(active -> {
                List<PriceData> prices = priceDataRepository.findByActiveIdOrderByDateAsc(id);
                
                List<Map<String, Object>> pricesData = prices.stream().map(price -> 
                    Map.<String, Object>of(
                        "id", price.getId(),
                        "date", price.getDate().toString(),
                        "open", price.getOpen(),
                        "high", price.getHigh(),
                        "low", price.getLow(),
                        "close", price.getClose(),
                        "volume", price.getVolume()
                    )
                ).toList();

                return ResponseEntity.ok(Map.<String, Object>of(
                    "active", Map.of(
                        "id", active.getId(),
                        "name", active.getName(),
                        "symbol", active.getSymbol()
                    ),
                    "prices", pricesData,
                    "count", pricesData.size()
                ));
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
