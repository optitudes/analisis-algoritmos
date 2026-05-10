package com.example.demo.dto;

public record CosineSimilarityResultDTO(
    double similarity,
    String description,
    long executionTimeMs
) {}
