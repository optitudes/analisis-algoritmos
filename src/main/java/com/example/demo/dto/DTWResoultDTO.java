package com.example.demo.dto;

import java.util.List;

public record DTWResoultDTO(double totalCost, List<WarpingStep> warpingPath, Long executionTime, double[][] costTable) {}
