package com.example.demo.controllers;

import com.example.demo.dto.PatternResult;
import com.example.demo.dto.VolatilityRankingResponse;
import com.example.demo.dto.VolatilityResult;
import com.example.demo.servicios.interfaces.PatternAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el Requerimiento 3: análisis de frecuencia de
 * patrones y medición de volatilidad de activos financieros.
 *
 * <p>Expone los endpoints que permiten al frontend consumir los resultados
 * del análisis algorítmico de patrones (sliding window) y la clasificación
 * de riesgo basada en volatilidad histórica.</p>
 *
 * <h3>Endpoints disponibles:</h3>
 * <table>
 *   <tr><th>Método</th><th>Ruta</th><th>Descripción</th></tr>
 *   <tr><td>GET</td><td>/api/analysis/patterns/{activeId}</td>
 *       <td>Patrones para un activo</td></tr>
 *   <tr><td>GET</td><td>/api/analysis/patterns</td>
 *       <td>Patrones para todos los activos</td></tr>
 *   <tr><td>GET</td><td>/api/analysis/volatility/{activeId}</td>
 *       <td>Volatilidad de un activo</td></tr>
 *   <tr><td>GET</td><td>/api/analysis/risk-ranking</td>
 *       <td>Ranking de riesgo completo</td></tr>
 * </table>
 *
 * @see PatternAnalysisService
 */
@RestController
@RequestMapping("/api/analysis")
public class PatternAnalysisController {

    private final PatternAnalysisService analysisService;

    /**
     * Constructor con inyección de dependencias de Spring.
     *
     * @param analysisService servicio de análisis de patrones y volatilidad
     */
    public PatternAnalysisController(PatternAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    /**
     * Analiza los patrones de un activo específico usando ventana deslizante.
     *
     * <p>Ejecuta la detección de los dos patrones definidos (Bullish Streak
     * y Volume Spike Reversal) sobre el historial de precios del activo
     * indicado.</p>
     *
     * <h4>Ejemplo de uso:</h4>
     * <pre>GET /api/analysis/patterns/1?windowSize=10</pre>
     *
     * @param activeId   ID del activo financiero
     * @param windowSize tamaño de la ventana deslizante (default: 10 días).
     *                   Valores típicos: 5 (semanal), 10 (bisemanal),
     *                   20 (mensual), 60 (trimestral).
     * @return lista de resultados de patrones (2 elementos: uno por patrón),
     *         o 404 si no se encuentran resultados
     */
    @GetMapping("/patterns/{activeId}")
    public ResponseEntity<List<PatternResult>> analyzePatterns(
            @PathVariable Long activeId,
            @RequestParam(required = false, defaultValue = "10") int windowSize) {

        List<PatternResult> results = analysisService.analyzePatterns(activeId, windowSize);

        if (results.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(results);
    }

    /**
     * Analiza los patrones de todos los activos del portafolio.
     *
     * <p>Ejecuta el análisis de patrones con sliding window para cada activo
     * registrado en la base de datos. Los resultados se agrupan por símbolo
     * del activo.</p>
     *
     * <h4>Ejemplo de uso:</h4>
     * <pre>GET /api/analysis/patterns?windowSize=20</pre>
     *
     * @param windowSize tamaño de la ventana deslizante (default: 10 días)
     * @return mapa de símbolo → lista de resultados de patrones
     */
    @GetMapping("/patterns")
    public ResponseEntity<Map<String, List<PatternResult>>> analyzeAllPatterns(
            @RequestParam(required = false, defaultValue = "10") int windowSize) {

        Map<String, List<PatternResult>> results = analysisService.analyzeAllPatterns(windowSize);
        return ResponseEntity.ok(results);
    }

    /**
     * Calcula las métricas de volatilidad y clasificación de riesgo de un
     * activo específico.
     *
     * <p>Retorna la desviación estándar, volatilidad histórica anualizada
     * y la categoría de riesgo (CONSERVADOR, MODERADO, AGRESIVO) calculadas
     * a partir de los retornos logarítmicos diarios del activo.</p>
     *
     * <h4>Ejemplo de uso:</h4>
     * <pre>GET /api/analysis/volatility/1</pre>
     *
     * @param activeId ID del activo financiero
     * @return resultado de volatilidad con clasificación de riesgo,
     *         o 404 si el activo no existe o no tiene datos suficientes
     */
    @GetMapping("/volatility/{activeId}")
    public ResponseEntity<VolatilityResult> getVolatility(@PathVariable Long activeId) {
        VolatilityResult result = analysisService.calculateVolatility(activeId);

        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Genera el ranking completo de todos los activos del portafolio
     * ordenados por nivel de riesgo (de conservador a agresivo).
     *
     * <p>Este endpoint implementa el listado de activos ordenados según
     * su nivel de riesgo calculado de manera estrictamente algorítmica,
     * como lo requiere el Requerimiento 3.</p>
     *
     * <p>El ordenamiento se realiza con una implementación manual de
     * QuickSort sobre la volatilidad histórica anualizada de cada activo.</p>
     *
     * <h4>Ejemplo de uso:</h4>
     * <pre>GET /api/analysis/risk-ranking</pre>
     *
     * @return respuesta con el ranking ordenado y la distribución por categoría
     */
    @GetMapping("/risk-ranking")
    public ResponseEntity<VolatilityRankingResponse> getRiskRanking() {
        VolatilityRankingResponse response = analysisService.getRiskRanking();
        return ResponseEntity.ok(response);
    }
}
