package com.example.demo.servicios.interfaces;

import java.util.List;

import com.example.demo.dto.PearsonCorrelationResoultDTO;
import com.example.demo.dto.PriceDataProjection;

public interface TimeSeriesSimilarityService {
    /**
     * Obtiene los resultados del coeficiente de pearson.
     *
     * @param setX Representa el set de datos que va a interpretarse como x (primera variable).
     * @param setY Representa el set de datos que va a interpretarse como y (segunda variable).
     * @return Resultados con los tiempos.
     */
    PearsonCorrelationResoultDTO getPearsonCorrelation(List<PriceDataProjection> setX, List<PriceDataProjection> setY);
}
