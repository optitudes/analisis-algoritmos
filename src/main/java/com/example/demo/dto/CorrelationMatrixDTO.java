package com.example.demo.dto;

import java.util.List;

public record CorrelationMatrixDTO(
    List<String> labels,
    List<List<Double>> matrix,
    long executionTime
) {}
