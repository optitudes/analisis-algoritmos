package com.example.demo.servicios.interfaces;

import java.time.LocalDate;
import java.util.List;

public interface StockService {
    List<String> fetchHistoricalData(String symbol, LocalDate startDate);
}
