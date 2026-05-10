package com.example.demo.dto;

import java.util.List;

public record EuclideanDistanceResoultDTO(List<EuclideanDistancePointDTO> distanceList, Long executionTime) {}
