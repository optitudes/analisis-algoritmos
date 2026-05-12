package com.example.demo.controllers;

import com.example.demo.dto.CorrelationMatrixDTO;
import com.example.demo.servicios.interfaces.CorrelationMatrixService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/correlation-matrix")
public class CorrelationMatrixController {

    private final CorrelationMatrixService correlationMatrixService;

    public CorrelationMatrixController(CorrelationMatrixService correlationMatrixService) {
        this.correlationMatrixService = correlationMatrixService;
    }

    @GetMapping
    public ResponseEntity<CorrelationMatrixDTO> getCorrelationMatrix(
            @RequestParam(defaultValue = "close") String field) {
        CorrelationMatrixDTO result = correlationMatrixService.getCorrelationMatrix(field);
        return ResponseEntity.ok(result);
    }
}
