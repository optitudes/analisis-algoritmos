package com.example.demo.dto;

/**
 * DTO que encapsula las métricas de dispersión y la clasificación de riesgo
 * de un activo financiero individual (acción o ETF) del portafolio.
 *
 * <h3>Fundamentación matemática:</h3>
 *
 * <p><b>Retornos logarítmicos diarios:</b></p>
 * <pre>
 *   r_i = ln(close_i / close_{i-1})
 * </pre>
 * <p>Se utilizan retornos logarítmicos en lugar de retornos simples porque
 * son aditivos en el tiempo y se distribuyen de forma más cercana a la normal,
 * lo cual es un supuesto estándar en finanzas cuantitativas.</p>
 *
 * <p><b>Media aritmética de retornos:</b></p>
 * <pre>
 *   μ = (1/n) × Σ r_i,  para i = 1..n
 * </pre>
 *
 * <p><b>Desviación estándar muestral (corrección de Bessel):</b></p>
 * <pre>
 *   σ = √( (1/(n-1)) × Σ (r_i - μ)²,  para i = 1..n )
 * </pre>
 * <p>Se usa n-1 en el denominador (corrección de Bessel) porque se trabaja
 * con una muestra del proceso generador de datos, no con la población completa.</p>
 *
 * <p><b>Volatilidad histórica anualizada:</b></p>
 * <pre>
 *   σ_anual = σ_diaria × √252
 * </pre>
 * <p>252 corresponde al número aproximado de días hábiles de trading en un año.
 * La raíz cuadrada se aplica bajo el supuesto de que los retornos diarios son
 * independientes e idénticamente distribuidos (i.i.d.).</p>
 *
 * <p><b>Clasificación de riesgo:</b></p>
 * <ul>
 *   <li>CONSERVADOR: σ_anual &lt; 0.20 (20%)</li>
 *   <li>MODERADO:    0.20 ≤ σ_anual &lt; 0.40 (20% - 40%)</li>
 *   <li>AGRESIVO:    σ_anual ≥ 0.40 (40%+)</li>
 * </ul>
 * <p>Estos umbrales siguen la convención estándar de la industria financiera
 * para la clasificación de perfiles de riesgo de instrumentos individuales.</p>
 *
 * @see com.example.demo.util.PatternAnalysisAlgorithms
 */
public class VolatilityResult {

    /** Símbolo bursátil del activo analizado (e.g., "CIB", "EC"). */
    private String activeSymbol;

    /** Nombre descriptivo del activo (e.g., "Bancolombia - ADR"). */
    private String activeName;

    /**
     * Retorno diario promedio (media aritmética de los retornos logarítmicos).
     * Valor típicamente cercano a cero para la mayoría de activos.
     */
    private double meanDailyReturn;

    /**
     * Desviación estándar muestral de los retornos logarítmicos diarios.
     * Calculada con corrección de Bessel (denominador n-1).
     */
    private double standardDeviation;

    /**
     * Volatilidad histórica anualizada: {@code σ_diaria × √252}.
     * Expresada como decimal (e.g., 0.35 = 35%).
     */
    private double historicalVolatility;

    /**
     * Categoría de riesgo asignada algorítmicamente.
     * Valores posibles: "CONSERVADOR", "MODERADO", "AGRESIVO".
     */
    private String riskCategory;

    /**
     * Número de registros de precios utilizados para el cálculo.
     * Un valor mayor provee mayor confianza estadística en los resultados.
     */
    private int dataPointsUsed;

    /** Constructor vacío requerido para la serialización JSON de Spring. */
    public VolatilityResult() {
    }

    /**
     * Constructor completo para la creación de resultados de volatilidad.
     *
     * @param activeSymbol       símbolo del activo
     * @param activeName         nombre del activo
     * @param meanDailyReturn    retorno diario promedio
     * @param standardDeviation  desviación estándar de retornos diarios
     * @param historicalVolatility volatilidad histórica anualizada
     * @param riskCategory       categoría de riesgo ("CONSERVADOR", "MODERADO", "AGRESIVO")
     * @param dataPointsUsed     número de registros de precios utilizados
     */
    public VolatilityResult(String activeSymbol, String activeName, double meanDailyReturn,
                            double standardDeviation, double historicalVolatility,
                            String riskCategory, int dataPointsUsed) {
        this.activeSymbol = activeSymbol;
        this.activeName = activeName;
        this.meanDailyReturn = meanDailyReturn;
        this.standardDeviation = standardDeviation;
        this.historicalVolatility = historicalVolatility;
        this.riskCategory = riskCategory;
        this.dataPointsUsed = dataPointsUsed;
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

    public double getMeanDailyReturn() {
        return meanDailyReturn;
    }

    public void setMeanDailyReturn(double meanDailyReturn) {
        this.meanDailyReturn = meanDailyReturn;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public double getHistoricalVolatility() {
        return historicalVolatility;
    }

    public void setHistoricalVolatility(double historicalVolatility) {
        this.historicalVolatility = historicalVolatility;
    }

    public String getRiskCategory() {
        return riskCategory;
    }

    public void setRiskCategory(String riskCategory) {
        this.riskCategory = riskCategory;
    }

    public int getDataPointsUsed() {
        return dataPointsUsed;
    }

    public void setDataPointsUsed(int dataPointsUsed) {
        this.dataPointsUsed = dataPointsUsed;
    }
}
