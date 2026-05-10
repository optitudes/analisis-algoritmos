package com.example.demo.servicios.interfaces;

import java.util.List;

import com.example.demo.dto.CosineSimilarityResultDTO;
import com.example.demo.dto.DTWResoultDTO;
import com.example.demo.dto.EuclideanDistanceResoultDTO;
import com.example.demo.dto.PearsonCorrelationResoultDTO;
import com.example.demo.dto.PriceDataProjection;

public interface TimeSeriesSimilarityService {
    /**
     * Obtiene los resultados del coeficiente de pearson.
     *
     * @param setX Representa el set de datos que va a interpretarse como x (primera variable).
     * @param setY Representa el set de datos que va a interpretarse como y (segunda variable).
     * @return Resultado.
     */
    PearsonCorrelationResoultDTO getPearsonCorrelation(List<PriceDataProjection> setX, List<PriceDataProjection> setY);

    /**
     * Obtiene la lista de distancias euclideanas entre dos sets de datos.
     *
     * @param setX Representa el set de datos que va a interpretarse como x (primera variable).
     * @param setY Representa el set de datos que va a interpretarse como y (segunda variable).
     * @return Resultado.
     */
    EuclideanDistanceResoultDTO getEuclideanDistanceList(List<PriceDataProjection> setX, List<PriceDataProjection> setY);

    /**
     * Obtiene la mejor alineacion entre dos sets de datos.
     *
     * @param setX Representa el set de datos que va a interpretarse como x (primera variable).
     * @param setY Representa el set de datos que va a interpretarse como y (segunda variable).
     * @return Resultado.
     */
    DTWResoultDTO getDTW(List<PriceDataProjection> setX, List<PriceDataProjection> setY);

    /**
     * Obtiene la similitud por coseno.
     *
     * @param setX Representa el set de datos que va a interpretarse como x (primera variable).
     * @param setY Representa el set de datos que va a interpretarse como y (segunda variable).
     * @return Resultado.
     */
    CosineSimilarityResultDTO getCosineSimilarity(List<PriceDataProjection> setX, List<PriceDataProjection> setY);

}
