package com.example.demo.dto;

import java.time.LocalDate;

public record PriceDataProjection(LocalDate date, Number valueObtained) {}
