package com.example.demo.servicios.implementations;

import com.example.demo.dto.PatternResult;
import com.example.demo.dto.VolatilityRankingResponse;
import com.example.demo.dto.VolatilityResult;
import com.example.demo.entidades.Active;
import com.example.demo.entidades.PriceData;
import com.example.demo.repositorios.ActiveRepository;
import com.example.demo.repositorios.PriceDataRepository;
import com.example.demo.servicios.interfaces.PatternAnalysisService;
import com.example.demo.util.PatternAnalysisAlgorithms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación del servicio de análisis de patrones y volatilidad para
 * el Requerimiento 3 del proyecto.
 *
 * <p>Esta clase orquesta la interacción entre la capa de persistencia
 * (repositorios JPA) y los algoritmos implementados en
 * {@link PatternAnalysisAlgorithms}. Sigue el patrón de inyección de
 * dependencias por constructor, consistente con el resto del proyecto.</p>
 *
 * <h3>Responsabilidades:</h3>
 * <ol>
 *   <li>Recuperar los datos históricos de precios desde la base de datos</li>
 *   <li>Transformar las entidades JPA en arreglos primitivos para los algoritmos</li>
 *   <li>Delegar la lógica algorítmica a {@link PatternAnalysisAlgorithms}</li>
 *   <li>Construir los DTOs de respuesta con los resultados</li>
 *   <li>Ordenar el ranking de riesgo con QuickSort manual</li>
 * </ol>
 *
 * <h3>Declaración sobre el uso de IA:</h3>
 * <p>Se utilizó IA generativa como apoyo para la documentación técnica y la
 * revisión de la fundamentación matemática de las fórmulas. El diseño
 * algorítmico, la arquitectura del servicio y la implementación de la
 * lógica de negocio fueron realizados por los estudiantes.</p>
 *
 * @see PatternAnalysisService
 * @see PatternAnalysisAlgorithms
 */
@Service
public class PatternAnalysisServiceImpl implements PatternAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(PatternAnalysisServiceImpl.class);

    /**
     * Descripción formal del patrón Bullish Streak para documentación.
     * Se incluye la formalización matemática del patrón.
     */
    private static final String BULLISH_STREAK_DESCRIPTION =
            "Racha alcista: subsecuencia de al menos 3 días consecutivos donde "
                    + "close[j+1] > close[j]. Formalización: ∃ j ∈ [i, i+w-2] tal que "
                    + "close[j+1] > close[j] para al menos 3 j consecutivos.";

    /**
     * Descripción formal del patrón Volume Spike Reversal para documentación.
     */
    private static final String VOLUME_SPIKE_REVERSAL_DESCRIPTION =
            "Pico de volumen con reversión: dentro de la ventana, un día con volumen "
                    + "≥ 2× el promedio de la ventana, seguido de cambio de dirección en "
                    + "el precio. Formalización: ∃ j tal que volume[j] ≥ 2×μ_vol ∧ "
                    + "signo(Δclose_antes) ≠ signo(Δclose_después).";

    private final ActiveRepository activeRepository;
    private final PriceDataRepository priceDataRepository;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param activeRepository    repositorio de activos financieros
     * @param priceDataRepository repositorio de datos históricos de precios
     */
    public PatternAnalysisServiceImpl(ActiveRepository activeRepository,
                                      PriceDataRepository priceDataRepository) {
        this.activeRepository = activeRepository;
        this.priceDataRepository = priceDataRepository;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Flujo de ejecución:</p>
     * <ol>
     *   <li>Buscar el activo por ID en la base de datos</li>
     *   <li>Obtener los precios históricos ordenados por fecha ascendente</li>
     *   <li>Extraer precios de cierre y volúmenes a arreglos primitivos</li>
     *   <li>Ejecutar detección de Bullish Streak con sliding window</li>
     *   <li>Ejecutar detección de Volume Spike Reversal con sliding window</li>
     *   <li>Construir y retornar los DTOs de resultado</li>
     * </ol>
     */
    @Override
    public List<PatternResult> analyzePatterns(Long activeId, int windowSize) {
        List<PatternResult> results = new ArrayList<>();

        // 1. Buscar el activo en la base de datos
        Optional<Active> activeOpt = activeRepository.findById(activeId);
        if (activeOpt.isEmpty()) {
            logger.warn("Activo con ID {} no encontrado para análisis de patrones", activeId);
            return results;
        }
        Active active = activeOpt.get();

        // 2. Obtener los datos históricos de precios, ordenados por fecha ascendente
        List<PriceData> priceDataList = priceDataRepository.findByActiveIdOrderByDateAsc(activeId);
        if (priceDataList.size() < windowSize) {
            logger.warn("Datos insuficientes para {} (tiene {}, requiere {})",
                    active.getSymbol(), priceDataList.size(), windowSize);
            return results;
        }

        // 3. Extraer los datos a arreglos primitivos para los algoritmos
        double[] closePrices = PatternAnalysisAlgorithms.extractClosePrices(priceDataList);
        long[] volumes = PatternAnalysisAlgorithms.extractVolumes(priceDataList);

        // 4. Patrón 1: Bullish Streak (racha alcista ≥ 3 días consecutivos)
        List<Integer> bullishOccurrences = PatternAnalysisAlgorithms
                .detectBullishStreak(closePrices, windowSize);
        results.add(buildPatternResult(
                active, "Bullish Streak", BULLISH_STREAK_DESCRIPTION,
                windowSize, closePrices.length, bullishOccurrences, priceDataList
        ));

        // 5. Patrón 2: Volume Spike Reversal (pico de volumen con reversión)
        List<Integer> volumeSpikeOccurrences = PatternAnalysisAlgorithms
                .detectVolumeSpikeReversal(closePrices, volumes, windowSize);
        results.add(buildPatternResult(
                active, "Volume Spike Reversal", VOLUME_SPIKE_REVERSAL_DESCRIPTION,
                windowSize, closePrices.length, volumeSpikeOccurrences, priceDataList
        ));

        logger.info("Análisis de patrones completado para {} - Bullish: {}, VolSpike: {}",
                active.getSymbol(), bullishOccurrences.size(), volumeSpikeOccurrences.size());

        return results;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Itera sobre todos los activos registrados y ejecuta el análisis
     * de patrones individual para cada uno.</p>
     */
    @Override
    public Map<String, List<PatternResult>> analyzeAllPatterns(int windowSize) {
        Map<String, List<PatternResult>> allResults = new LinkedHashMap<>();

        List<Active> actives = activeRepository.findAll();
        logger.info("Iniciando análisis de patrones para {} activos con ventana de tamaño {}",
                actives.size(), windowSize);

        for (Active active : actives) {
            List<PatternResult> patterns = analyzePatterns(active.getId(), windowSize);
            if (!patterns.isEmpty()) {
                allResults.put(active.getSymbol(), patterns);
            }
        }

        logger.info("Análisis de patrones completado para {} activos", allResults.size());
        return allResults;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Flujo de cálculo:</p>
     * <ol>
     *   <li>Obtener precios de cierre del activo</li>
     *   <li>Calcular retornos logarítmicos: r_i = ln(close_i / close_{i-1})</li>
     *   <li>Calcular media aritmética: μ = (1/n) × Σr_i</li>
     *   <li>Calcular desviación estándar muestral: σ = √((1/(n-1)) × Σ(r_i - μ)²)</li>
     *   <li>Anualizar la volatilidad: σ_anual = σ_diaria × √252</li>
     *   <li>Clasificar el riesgo según umbrales estándar</li>
     * </ol>
     */
    @Override
    public VolatilityResult calculateVolatility(Long activeId) {
        // 1. Buscar el activo en la base de datos
        Optional<Active> activeOpt = activeRepository.findById(activeId);
        if (activeOpt.isEmpty()) {
            logger.warn("Activo con ID {} no encontrado para cálculo de volatilidad", activeId);
            return null;
        }
        Active active = activeOpt.get();

        // 2. Obtener los datos históricos ordenados cronológicamente
        List<PriceData> priceDataList = priceDataRepository.findByActiveIdOrderByDateAsc(activeId);
        if (priceDataList.size() < 2) {
            logger.warn("Datos insuficientes para calcular volatilidad de {} (tiene {})",
                    active.getSymbol(), priceDataList.size());
            return null;
        }

        // 3. Extraer precios de cierre a arreglo primitivo
        double[] closePrices = PatternAnalysisAlgorithms.extractClosePrices(priceDataList);

        // 4. Calcular retornos logarítmicos: r_i = ln(close_i / close_{i-1})
        double[] logReturns = PatternAnalysisAlgorithms.calculateLogReturns(closePrices);

        // 5. Calcular media aritmética de los retornos
        double meanReturn = PatternAnalysisAlgorithms.calculateMean(logReturns);

        // 6. Calcular desviación estándar muestral (corrección de Bessel)
        double stdDev = PatternAnalysisAlgorithms.calculateStandardDeviation(logReturns, meanReturn);

        // 7. Anualizar la volatilidad
        double annualizedVolatility = PatternAnalysisAlgorithms.calculateAnnualizedVolatility(stdDev);

        // 8. Clasificar el riesgo
        String riskCategory = PatternAnalysisAlgorithms.classifyRisk(annualizedVolatility);

        logger.info("Volatilidad de {}: σ_diaria={}, σ_anual={} ({})",
                active.getSymbol(), stdDev, annualizedVolatility, riskCategory);

        return new VolatilityResult(
                active.getSymbol(),
                active.getName(),
                meanReturn,
                stdDev,
                annualizedVolatility,
                riskCategory,
                closePrices.length
        );
    }

    /**
     * {@inheritDoc}
     *
     * <p>Flujo de ejecución:</p>
     * <ol>
     *   <li>Calcular la volatilidad de cada activo del portafolio</li>
     *   <li>Ordenar los resultados por volatilidad usando QuickSort manual</li>
     *   <li>Contabilizar la distribución por categoría de riesgo</li>
     *   <li>Construir la respuesta envoltorio</li>
     * </ol>
     */
    @Override
    public VolatilityRankingResponse getRiskRanking() {
        List<Active> actives = activeRepository.findAll();
        List<VolatilityResult> volatilityResults = new ArrayList<>();

        logger.info("Iniciando cálculo de ranking de riesgo para {} activos", actives.size());

        // 1. Calcular la volatilidad de cada activo
        for (Active active : actives) {
            VolatilityResult result = calculateVolatility(active.getId());
            if (result != null) {
                volatilityResults.add(result);
            }
        }

        // 2. Ordenar por volatilidad de menor a mayor usando QuickSort manual
        VolatilityResult[] resultsArray = volatilityResults.toArray(new VolatilityResult[0]);
        PatternAnalysisAlgorithms.quickSortVolatility(resultsArray);

        // Convertir de vuelta a lista manteniendo el orden
        List<VolatilityResult> sortedResults = new ArrayList<>();
        for (VolatilityResult result : resultsArray) {
            sortedResults.add(result);
        }

        // 3. Contabilizar la distribución por categoría de riesgo
        Map<String, Integer> categoryDistribution = new HashMap<>();
        categoryDistribution.put("CONSERVADOR", 0);
        categoryDistribution.put("MODERADO", 0);
        categoryDistribution.put("AGRESIVO", 0);

        for (VolatilityResult result : sortedResults) {
            String category = result.getRiskCategory();
            categoryDistribution.put(category, categoryDistribution.get(category) + 1);
        }

        logger.info("Ranking de riesgo completado: {} activos - Conservadores: {}, Moderados: {}, Agresivos: {}",
                sortedResults.size(),
                categoryDistribution.get("CONSERVADOR"),
                categoryDistribution.get("MODERADO"),
                categoryDistribution.get("AGRESIVO"));

        // 4. Construir la respuesta envoltorio
        return new VolatilityRankingResponse(sortedResults, sortedResults.size(), categoryDistribution);
    }

    // =====================================================================
    // MÉTODOS PRIVADOS AUXILIARES
    // =====================================================================

    /**
     * Construye un {@link PatternResult} a partir de los datos del análisis.
     *
     * <p>Calcula la frecuencia porcentual y extrae las fechas de inicio
     * de cada ventana donde se detectó el patrón.</p>
     *
     * @param active             activo analizado
     * @param patternName        nombre del patrón
     * @param patternDescription descripción formal del patrón
     * @param windowSize         tamaño de la ventana
     * @param totalDataPoints    total de puntos de datos de precios
     * @param occurrenceIndices  índices de inicio de las ventanas con ocurrencias
     * @param priceDataList      lista completa de datos de precios (para extraer fechas)
     * @return DTO con el resultado del análisis del patrón
     */
    private PatternResult buildPatternResult(Active active, String patternName,
                                             String patternDescription, int windowSize,
                                             int totalDataPoints, List<Integer> occurrenceIndices,
                                             List<PriceData> priceDataList) {
        int totalWindows = totalDataPoints - windowSize + 1;
        int occurrences = occurrenceIndices.size();

        // Calcular la frecuencia porcentual: (ocurrencias / totalVentanas) × 100
        double frequency = totalWindows > 0
                ? (occurrences * 100.0) / totalWindows
                : 0.0;

        // Extraer las fechas de inicio de cada ventana con ocurrencia
        List<String> occurrenceDates = new ArrayList<>();
        for (int index : occurrenceIndices) {
            if (index < priceDataList.size()) {
                occurrenceDates.add(priceDataList.get(index).getDate().toString());
            }
        }

        return new PatternResult(
                active.getSymbol(),
                active.getName(),
                patternName,
                patternDescription,
                windowSize,
                totalWindows,
                occurrences,
                frequency,
                occurrenceDates
        );
    }
}
