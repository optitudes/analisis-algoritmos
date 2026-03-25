package com.example.demo.controllers;

import com.example.demo.dto.SortingResult;
import com.example.demo.entidades.PriceData;
import com.example.demo.servicios.interfaces.SortingAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sorting")
public class SortingController {

    private final SortingAnalysisService analysisService;

    public SortingController(SortingAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    /**
     * Ejecuta el análisis general midiendo los tiempos de los 12 algoritmos
     * operando sobre la tabla de reales generada de PriceData.
     * Ejemplo de uso limitando la carga de datos a 1000 iteraciones para no bloquear por tiempo: /api/sorting/analysis?limit=1000
     */
    @GetMapping("/analysis")
    public ResponseEntity<List<SortingResult>> getSortingAnalysis(
            @RequestParam(required = false, defaultValue = "-1") int limit) {
        List<SortingResult> results = analysisService.runAnalysis(limit);
        return ResponseEntity.ok(results);
    }

    /**
     * Endpoint solicitado para retornar los 15 mayores volúmenes ordenados
     * ascendentemente de acuerdo con las instrucciones dadas.
     */
    @GetMapping("/top-volume-days")
    public ResponseEntity<List<PriceData>> getTopVolumeDays() {
        return ResponseEntity.ok(analysisService.getTop15DaysByVolume());
    }
}
