package com.example.demo.servicios.interfaces;

import com.example.demo.dto.PatternResult;
import com.example.demo.dto.VolatilityRankingResponse;
import com.example.demo.dto.VolatilityResult;

import java.util.List;
import java.util.Map;

/**
 * Interfaz de servicio para el Requerimiento 3: análisis de frecuencia de
 * patrones mediante ventana deslizante y medición de volatilidad para
 * la clasificación de riesgo de activos financieros.
 *
 * <p>Esta interfaz define las operaciones de negocio que conectan los
 * algoritmos implementados en {@link com.example.demo.util.PatternAnalysisAlgorithms}
 * con los datos persistidos en la base de datos (entidades {@code Active}
 * y {@code PriceData}).</p>
 *
 * @see com.example.demo.servicios.implementations.PatternAnalysisServiceImpl
 * @see com.example.demo.util.PatternAnalysisAlgorithms
 */
public interface PatternAnalysisService {

    /**
     * Ejecuta el análisis de patrones con ventana deslizante para un activo
     * específico del portafolio.
     *
     * <p>Se aplican ambos patrones definidos (Bullish Streak y Volume Spike
     * Reversal) sobre el historial de precios del activo. Se retorna un
     * resultado por cada patrón.</p>
     *
     * @param activeId   ID del activo en la base de datos
     * @param windowSize tamaño de la ventana deslizante (mínimo recomendado: 5)
     * @return lista con los resultados de cada patrón (2 elementos),
     *         o lista vacía si el activo no existe o no tiene datos suficientes
     */
    List<PatternResult> analyzePatterns(Long activeId, int windowSize);

    /**
     * Ejecuta el análisis de patrones con ventana deslizante para todos los
     * activos registrados en el portafolio.
     *
     * <p>El resultado se organiza como un mapa donde la clave es el símbolo
     * del activo y el valor es la lista de resultados de patrones de ese activo.</p>
     *
     * @param windowSize tamaño de la ventana deslizante
     * @return mapa de símbolo → lista de resultados de patrones
     */
    Map<String, List<PatternResult>> analyzeAllPatterns(int windowSize);

    /**
     * Calcula las métricas de volatilidad y clasificación de riesgo para
     * un activo específico del portafolio.
     *
     * <p>Se calculan: retornos logarítmicos, media, desviación estándar,
     * volatilidad histórica anualizada y categoría de riesgo.</p>
     *
     * @param activeId ID del activo en la base de datos
     * @return resultado de volatilidad con la clasificación de riesgo,
     *         o {@code null} si el activo no existe o no tiene datos suficientes
     */
    VolatilityResult calculateVolatility(Long activeId);

    /**
     * Calcula la volatilidad para todos los activos del portafolio y genera
     * un ranking ordenado de menor a mayor riesgo.
     *
     * <p>Incluye la distribución de activos por categoría de riesgo
     * (conservador, moderado, agresivo) y el listado ordenado usando
     * QuickSort manual.</p>
     *
     * @return respuesta con el ranking completo y la distribución por categoría
     */
    VolatilityRankingResponse getRiskRanking();
}
