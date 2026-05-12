package com.example.demo.dto;

import java.util.List;

/**
 * DTO que encapsula el resultado del análisis de un patrón detectado
 * mediante el algoritmo de ventana deslizante (sliding window) sobre
 * la serie temporal de precios de un activo financiero.
 *
 * <p>Cada instancia representa la frecuencia de aparición de un patrón
 * específico dentro del historial de precios de un activo individual.</p>
 *
 * <h3>Fundamentación algorítmica:</h3>
 * <p>El algoritmo de ventana deslizante recorre la serie de precios con una
 * ventana de tamaño fijo {@code w}, evaluando en cada posición {@code i}
 * (donde {@code 0 ≤ i ≤ n - w}) si el patrón definido se cumple dentro
 * del sub-arreglo {@code [i, i + w - 1]}. La frecuencia se calcula como:</p>
 * <pre>
 *   frecuencia(%) = (ocurrencias / totalVentanas) × 100
 * </pre>
 *
 * <p>Complejidad temporal del recorrido: O(n × w), donde n es el número
 * de registros de precios y w es el tamaño de la ventana.</p>
 *
 * @see com.example.demo.util.PatternAnalysisAlgorithms
 */
public class PatternResult {

    /** Símbolo bursátil del activo analizado (e.g., "CIB", "EC"). */
    private String activeSymbol;

    /** Nombre descriptivo del activo (e.g., "Bancolombia - ADR"). */
    private String activeName;

    /**
     * Nombre del patrón detectado.
     * Valores posibles: "Bullish Streak", "Volume Spike Reversal".
     */
    private String patternName;

    /** Descripción formal del patrón para fines de documentación técnica. */
    private String patternDescription;

    /** Tamaño de la ventana deslizante utilizada en el análisis. */
    private int windowSize;

    /**
     * Total de ventanas evaluadas durante el recorrido.
     * Se calcula como: {@code n - windowSize + 1}, donde n es el tamaño de la serie.
     */
    private int totalWindows;

    /** Número de ventanas en las que se detectó el patrón. */
    private int patternOccurrences;

    /**
     * Porcentaje de frecuencia del patrón: {@code (patternOccurrences / totalWindows) × 100}.
     * Valor entre 0.0 y 100.0.
     */
    private double frequencyPercentage;

    /**
     * Lista de fechas (formato ISO 8601) correspondientes al inicio de cada
     * ventana donde se detectó el patrón. Permite la trazabilidad temporal
     * de las ocurrencias.
     */
    private List<String> occurrenceDates;

    /** Constructor vacío requerido para la serialización JSON de Spring. */
    public PatternResult() {
    }

    /**
     * Constructor completo para la creación de resultados de análisis de patrones.
     *
     * @param activeSymbol       símbolo del activo
     * @param activeName         nombre del activo
     * @param patternName        nombre del patrón
     * @param patternDescription descripción formal del patrón
     * @param windowSize         tamaño de la ventana deslizante
     * @param totalWindows       total de ventanas evaluadas
     * @param patternOccurrences ocurrencias detectadas
     * @param frequencyPercentage porcentaje de frecuencia
     * @param occurrenceDates    fechas de cada ocurrencia
     */
    public PatternResult(String activeSymbol, String activeName, String patternName,
                         String patternDescription, int windowSize, int totalWindows,
                         int patternOccurrences, double frequencyPercentage,
                         List<String> occurrenceDates) {
        this.activeSymbol = activeSymbol;
        this.activeName = activeName;
        this.patternName = patternName;
        this.patternDescription = patternDescription;
        this.windowSize = windowSize;
        this.totalWindows = totalWindows;
        this.patternOccurrences = patternOccurrences;
        this.frequencyPercentage = frequencyPercentage;
        this.occurrenceDates = occurrenceDates;
    }

    public String getActiveSymbol() {
        return activeSymbol;
    }

    public void setActiveSymbol(String activeSymbol) {
        this.activeSymbol = activeSymbol;
    }

    public String getActiveName() {
        return activeName;
    }

    public void setActiveName(String activeName) {
        this.activeName = activeName;
    }

    public String getPatternName() {
        return patternName;
    }

    public void setPatternName(String patternName) {
        this.patternName = patternName;
    }

    public String getPatternDescription() {
        return patternDescription;
    }

    public void setPatternDescription(String patternDescription) {
        this.patternDescription = patternDescription;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public int getTotalWindows() {
        return totalWindows;
    }

    public void setTotalWindows(int totalWindows) {
        this.totalWindows = totalWindows;
    }

    public int getPatternOccurrences() {
        return patternOccurrences;
    }

    public void setPatternOccurrences(int patternOccurrences) {
        this.patternOccurrences = patternOccurrences;
    }

    public double getFrequencyPercentage() {
        return frequencyPercentage;
    }

    public void setFrequencyPercentage(double frequencyPercentage) {
        this.frequencyPercentage = frequencyPercentage;
    }

    public List<String> getOccurrenceDates() {
        return occurrenceDates;
    }

    public void setOccurrenceDates(List<String> occurrenceDates) {
        this.occurrenceDates = occurrenceDates;
    }
}
