package com.example.demo.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO envoltorio que encapsula la respuesta completa del ranking de activos
 * ordenados por nivel de riesgo, calculado a partir de la volatilidad histórica.
 *
 * <p>Esta clase agrupa el listado ordenado de activos junto con metadata
 * estadística sobre la distribución de categorías de riesgo en el portafolio.</p>
 *
 * <h3>Criterio de ordenamiento:</h3>
 * <p>Los activos se ordenan de menor a mayor volatilidad histórica anualizada
 * usando una implementación manual del algoritmo QuickSort. Esto produce un
 * listado donde los activos conservadores aparecen primero y los agresivos
 * al final, facilitando la toma de decisiones de inversión.</p>
 *
 * <p>Complejidad temporal del ordenamiento: O(n log n) en caso promedio,
 * O(n²) en el peor caso (QuickSort).</p>
 *
 * @see VolatilityResult
 * @see com.example.demo.util.PatternAnalysisAlgorithms#quickSortVolatility
 */
public class VolatilityRankingResponse {

    /**
     * Lista de activos ordenados ascendentemente por volatilidad histórica.
     * Los activos de menor riesgo (conservadores) aparecen primero.
     */
    private List<VolatilityResult> ranking;

    /** Total de activos analizados en el portafolio. */
    private int totalActives;

    /**
     * Distribución de activos por categoría de riesgo.
     * Ejemplo: {"CONSERVADOR": 5, "MODERADO": 10, "AGRESIVO": 5}
     */
    private Map<String, Integer> categoryDistribution;

    /** Constructor vacío requerido para la serialización JSON de Spring. */
    public VolatilityRankingResponse() {
    }

    /**
     * Constructor completo para la creación de la respuesta de ranking.
     *
     * @param ranking              lista de activos ordenados por volatilidad
     * @param totalActives         total de activos analizados
     * @param categoryDistribution distribución por categoría de riesgo
     */
    public VolatilityRankingResponse(List<VolatilityResult> ranking, int totalActives,
                                     Map<String, Integer> categoryDistribution) {
        this.ranking = ranking;
        this.totalActives = totalActives;
        this.categoryDistribution = categoryDistribution;
    }

    public List<VolatilityResult> getRanking() {
        return ranking;
    }

    public void setRanking(List<VolatilityResult> ranking) {
        this.ranking = ranking;
    }

    public int getTotalActives() {
        return totalActives;
    }

    public void setTotalActives(int totalActives) {
        this.totalActives = totalActives;
    }

    public Map<String, Integer> getCategoryDistribution() {
        return categoryDistribution;
    }

    public void setCategoryDistribution(Map<String, Integer> categoryDistribution) {
        this.categoryDistribution = categoryDistribution;
    }
}
