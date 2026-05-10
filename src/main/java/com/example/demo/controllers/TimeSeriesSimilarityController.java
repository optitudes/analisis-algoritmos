package com.example.demo.controllers;

import com.example.demo.dto.PearsonCorrelationResoultDTO;
import com.example.demo.dto.PriceDataProjection;
import com.example.demo.entidades.Active;
import com.example.demo.entidades.PriceData;
import com.example.demo.repositorios.ActiveRepository;
import com.example.demo.repositorios.PriceDataRepository;
import com.example.demo.servicios.implementations.TimeSeriesSimilarityServiceImpl;
import com.example.demo.servicios.interfaces.TimeSeriesSimilarityService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/time-series-similarity")
public class TimeSeriesSimilarityController {

    private final ActiveRepository activeRepository;
    private final PriceDataRepository priceDataRepository;
    private final TimeSeriesSimilarityServiceImpl timeSeriesSimilarityServiceImpl;

    public TimeSeriesSimilarityController(ActiveRepository activeRepository, PriceDataRepository priceDataRepository, TimeSeriesSimilarityServiceImpl timeSeriesSimilarityServiceImpl) {
        this.activeRepository = activeRepository;
        this.priceDataRepository = priceDataRepository;
        this.timeSeriesSimilarityServiceImpl = timeSeriesSimilarityServiceImpl; 
    }

    @GetMapping("/process")
    public ResponseEntity<Map<String, Object>> getAllActives(
            @RequestParam Long firstActiveId,
            @RequestParam Long secondActiveId,
            @RequestParam String field) {

        List<PriceDataProjection> firstActivePriceData = priceDataRepository
            .fetchByActiveIdAndColumn(firstActiveId, field);
        List<PriceDataProjection> secondActivePriceData = priceDataRepository
            .fetchByActiveIdAndColumn(secondActiveId, field);

        PearsonCorrelationResoultDTO pearsonCorrelationResoult= timeSeriesSimilarityServiceImpl.getPearsonCorrelation(firstActivePriceData, secondActivePriceData);

        return ResponseEntity.ok(Map.of(
            "pearson", pearsonCorrelationResoult
        ));
    }
}
