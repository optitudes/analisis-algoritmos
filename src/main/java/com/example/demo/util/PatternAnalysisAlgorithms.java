package com.example.demo.util;

import com.example.demo.dto.VolatilityResult;
import com.example.demo.entidades.PriceData;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase utilitaria que contiene las implementaciones algorítmicas del
 * Requerimiento 3: análisis de frecuencia de patrones mediante ventana
 * deslizante (sliding window) y cálculo de métricas de dispersión para
 * la clasificación de riesgo de activos financieros.
 *
 * <h3>Restricciones de implementación:</h3>
 * <ul>
 *   <li>Todos los algoritmos están implementados de forma explícita usando
 *       estructuras básicas del lenguaje Java.</li>
 *   <li>No se utilizan librerías de alto nivel para estadística, análisis
 *       de series temporales ni aprendizaje automático.</li>
 *   <li>Las únicas funciones externas usadas son {@link Math#sqrt},
 *       {@link Math#log} y {@link Math#abs}, que son operaciones
 *       matemáticas primitivas del lenguaje.</li>
 * </ul>
 *
 * <h3>Declaración sobre el uso de IA:</h3>
 * <p>Se utilizó inteligencia artificial generativa como apoyo para la
 * documentación y revisión de la fundamentación matemática. El diseño
 * algorítmico y la implementación fueron realizados por los estudiantes.</p>
 *
 * @see com.example.demo.dto.PatternResult
 * @see com.example.demo.dto.VolatilityResult
 */
public final class PatternAnalysisAlgorithms {

    // =====================================================================
    // Constantes
    // =====================================================================

    /**
     * Número de días hábiles de trading por año.
     * Se usa para anualizar la volatilidad diaria: σ_anual = σ_diaria × √252.
     */
    private static final int TRADING_DAYS_PER_YEAR = 252;

    /**
     * Mínimo de días consecutivos al alza para considerar un Bullish Streak.
     * Una racha de al menos 3 días se considera estadísticamente significativa.
     */
    private static final int MIN_BULLISH_STREAK_LENGTH = 3;

    /**
     * Factor multiplicador para la detección de picos de volumen.
     * Un volumen ≥ 2× el promedio de la ventana se considera un spike.
     */
    private static final double VOLUME_SPIKE_FACTOR = 2.0;

    /** Umbral superior para la categoría de riesgo CONSERVADOR (20%). */
    private static final double CONSERVATIVE_THRESHOLD = 0.20;

    /** Umbral superior para la categoría de riesgo MODERADO (40%). */
    private static final double MODERATE_THRESHOLD = 0.40;

    /** Constructor privado para evitar la instanciación de clase utilitaria. */
    private PatternAnalysisAlgorithms() {
        throw new UnsupportedOperationException("Clase utilitaria: no se debe instanciar");
    }

    // =====================================================================
    // PATRÓN 1: Bullish Streak (Días consecutivos al alza)
    // =====================================================================

    /**
     * Detecta el patrón "Bullish Streak" (racha alcista) dentro de una ventana
     * de precios de cierre usando el algoritmo de ventana deslizante.
     *
     * <h4>Formalización del patrón:</h4>
     * <p>Dada una ventana de precios de cierre {@code [close_i, close_{i+1}, ..., close_{i+w-1}]},
     * el patrón se detecta si existe una subsecuencia de al menos
     * {@value #MIN_BULLISH_STREAK_LENGTH} días consecutivos donde:</p>
     * <pre>
     *   ∃ j ∈ [0, w-2] tal que close[j+1] > close[j]
     *   para al menos MIN_BULLISH_STREAK_LENGTH valores consecutivos de j
     * </pre>
     *
     * <h4>Complejidad:</h4>
     * <ul>
     *   <li>Temporal: O(n × w) donde n = tamaño de la serie, w = tamaño de ventana</li>
     *   <li>Espacial: O(1) adicional (análisis in-place sobre la ventana)</li>
     * </ul>
     *
     * @param closePrices arreglo de precios de cierre ordenados cronológicamente
     * @param windowSize  tamaño de la ventana deslizante (debe ser ≥ MIN_BULLISH_STREAK_LENGTH + 1)
     * @return lista de índices donde inicia cada ventana con patrón detectado
     */
    public static List<Integer> detectBullishStreak(double[] closePrices, int windowSize) {
        List<Integer> occurrences = new ArrayList<>();

        // Validación: se necesitan al menos windowSize datos y la ventana debe
        // ser suficientemente grande para contener el patrón
        if (closePrices == null || closePrices.length < windowSize
                || windowSize < MIN_BULLISH_STREAK_LENGTH + 1) {
            return occurrences;
        }

        int totalData = closePrices.length;

        // Recorrido con ventana deslizante: evaluar cada posición posible
        // La ventana se desplaza desde la posición 0 hasta (n - windowSize)
        for (int windowStart = 0; windowStart <= totalData - windowSize; windowStart++) {

            // Dentro de la ventana actual, buscar una subsecuencia de ≥3 días al alza
            if (containsBullishStreak(closePrices, windowStart, windowSize)) {
                occurrences.add(windowStart);
            }
        }

        return occurrences;
    }

    /**
     * Verifica si una ventana específica contiene una racha alcista de al menos
     * {@value #MIN_BULLISH_STREAK_LENGTH} días consecutivos.
     *
     * <p>Se recorre la ventana de forma lineal contando incrementos consecutivos.
     * Cuando se detecta una caída o estancamiento, el contador se reinicia.</p>
     *
     * @param closePrices arreglo completo de precios de cierre
     * @param windowStart índice de inicio de la ventana
     * @param windowSize  tamaño de la ventana
     * @return {@code true} si el patrón se detecta dentro de la ventana
     */
    private static boolean containsBullishStreak(double[] closePrices, int windowStart, int windowSize) {
        // consecutiveUps cuenta los días consecutivos donde el precio subió
        int consecutiveUps = 0;

        // Iterar sobre los pares consecutivos dentro de la ventana
        for (int j = windowStart; j < windowStart + windowSize - 1; j++) {
            if (closePrices[j + 1] > closePrices[j]) {
                consecutiveUps++;
                // Si alcanzamos el mínimo requerido, el patrón existe en esta ventana
                if (consecutiveUps >= MIN_BULLISH_STREAK_LENGTH) {
                    return true;
                }
            } else {
                // Se rompe la racha: reiniciar contador
                consecutiveUps = 0;
            }
        }

        return false;
    }

    // =====================================================================
    // PATRÓN 2: Volume Spike Reversal (Pico de volumen con reversión)
    // =====================================================================

    /**
     * Detecta el patrón "Volume Spike Reversal" dentro de una ventana de datos
     * de precios y volúmenes usando el algoritmo de ventana deslizante.
     *
     * <h4>Formalización del patrón:</h4>
     * <p>Dada una ventana de datos {@code [data_i, ..., data_{i+w-1}]},
     * el patrón se detecta si:</p>
     * <pre>
     *   1. μ_vol = promedio(volume[i..i+w-1])
     *   2. ∃ j ∈ [i+1, i+w-2] tal que:
     *      a) volume[j] ≥ VOLUME_SPIKE_FACTOR × μ_vol  (pico de volumen)
     *      b) signo(close[j] - close[j-1]) ≠ signo(close[j+1] - close[j])  (reversión)
     * </pre>
     *
     * <h4>Interpretación financiera:</h4>
     * <p>Un pico de volumen acompañado de un cambio de dirección en el precio
     * señala un posible punto de inflexión (turning point) del mercado.
     * Esto puede indicar una capitulación, toma de ganancias masiva o entrada
     * institucional significativa que revierte la tendencia previa.</p>
     *
     * <h4>Complejidad:</h4>
     * <ul>
     *   <li>Temporal: O(n × w) donde n = tamaño de la serie, w = tamaño de ventana</li>
     *   <li>Espacial: O(1) adicional</li>
     * </ul>
     *
     * @param closePrices arreglo de precios de cierre ordenados cronológicamente
     * @param volumes     arreglo de volúmenes correspondientes a cada fecha
     * @param windowSize  tamaño de la ventana deslizante (debe ser ≥ 3)
     * @return lista de índices donde inicia cada ventana con patrón detectado
     */
    public static List<Integer> detectVolumeSpikeReversal(double[] closePrices, long[] volumes, int windowSize) {
        List<Integer> occurrences = new ArrayList<>();

        // Validación de entrada
        if (closePrices == null || volumes == null
                || closePrices.length != volumes.length
                || closePrices.length < windowSize || windowSize < 3) {
            return occurrences;
        }

        int totalData = closePrices.length;

        // Recorrido con ventana deslizante
        for (int windowStart = 0; windowStart <= totalData - windowSize; windowStart++) {

            if (containsVolumeSpikeReversal(closePrices, volumes, windowStart, windowSize)) {
                occurrences.add(windowStart);
            }
        }

        return occurrences;
    }

    /**
     * Verifica si una ventana específica contiene un pico de volumen con reversión.
     *
     * <p>Primero calcula el volumen promedio de la ventana. Luego busca algún día
     * interior (no el primero ni el último) que cumpla ambas condiciones:
     * volumen extraordinario Y reversión de tendencia del precio.</p>
     *
     * @param closePrices arreglo completo de precios de cierre
     * @param volumes     arreglo completo de volúmenes
     * @param windowStart índice de inicio de la ventana
     * @param windowSize  tamaño de la ventana
     * @return {@code true} si el patrón se detecta dentro de la ventana
     */
    private static boolean containsVolumeSpikeReversal(double[] closePrices, long[] volumes,
                                                       int windowStart, int windowSize) {
        // Paso 1: Calcular el volumen promedio de la ventana actual
        // μ_vol = (1/w) × Σ volume[j], para j = windowStart..windowStart+windowSize-1
        double volumeSum = 0.0;
        for (int j = windowStart; j < windowStart + windowSize; j++) {
            volumeSum += volumes[j];
        }
        double avgVolume = volumeSum / windowSize;

        // Evitar divisiones por cero si todos los volúmenes son cero
        if (avgVolume == 0.0) {
            return false;
        }

        // Paso 2: Buscar un día interior con spike de volumen y reversión de precio
        // Se excluyen el primer y último día de la ventana porque se necesitan
        // los vecinos (j-1) y (j+1) para verificar la reversión
        for (int j = windowStart + 1; j < windowStart + windowSize - 1; j++) {

            // Condición a): Volumen del día j ≥ VOLUME_SPIKE_FACTOR × promedio
            boolean isVolumeSpike = volumes[j] >= VOLUME_SPIKE_FACTOR * avgVolume;

            if (isVolumeSpike) {
                // Condición b): Reversión de dirección del precio
                // Calcular las diferencias de precio antes y después del spike
                double priceChangeBefore = closePrices[j] - closePrices[j - 1];
                double priceChangeAfter = closePrices[j + 1] - closePrices[j];

                // La reversión ocurre cuando los signos son opuestos:
                // - Si antes subía (priceChangeBefore > 0) y después baja (priceChangeAfter < 0)
                // - Si antes bajaba (priceChangeBefore < 0) y después sube (priceChangeAfter > 0)
                // Se ignoran los casos donde alguno de los cambios es exactamente 0
                boolean isReversal = (priceChangeBefore > 0 && priceChangeAfter < 0)
                        || (priceChangeBefore < 0 && priceChangeAfter > 0);

                if (isReversal) {
                    return true;
                }
            }
        }

        return false;
    }

    // =====================================================================
    // MÉTRICAS DE DISPERSIÓN: Desviación estándar y Volatilidad histórica
    // =====================================================================

    /**
     * Calcula los retornos logarítmicos diarios a partir de los precios de cierre.
     *
     * <p>El retorno logarítmico entre dos días consecutivos se define como:</p>
     * <pre>
     *   r_i = ln(close_i / close_{i-1})
     * </pre>
     *
     * <p>Ventajas de los retornos logarítmicos sobre los retornos simples:</p>
     * <ul>
     *   <li><b>Aditividad temporal:</b> r(t1, t3) = r(t1, t2) + r(t2, t3)</li>
     *   <li><b>Simetría:</b> Un retorno de +x% y -x% se cancelan exactamente</li>
     *   <li><b>Normalidad:</b> Se aproximan mejor a una distribución normal</li>
     * </ul>
     *
     * <p>Complejidad: O(n) temporal, O(n) espacial.</p>
     *
     * @param closePrices arreglo de precios de cierre ordenados cronológicamente.
     *                    Debe tener al menos 2 elementos.
     * @return arreglo de retornos logarítmicos de tamaño (closePrices.length - 1),
     *         o arreglo vacío si la entrada es insuficiente
     */
    public static double[] calculateLogReturns(double[] closePrices) {
        if (closePrices == null || closePrices.length < 2) {
            return new double[0];
        }

        int n = closePrices.length;
        double[] returns = new double[n - 1];

        for (int i = 1; i < n; i++) {
            // Proteger contra precios nulos o cero que harían indefinido el logaritmo
            if (closePrices[i] > 0 && closePrices[i - 1] > 0) {
                returns[i - 1] = Math.log(closePrices[i] / closePrices[i - 1]);
            } else {
                // Si algún precio es ≤ 0, el retorno se define como 0
                returns[i - 1] = 0.0;
            }
        }

        return returns;
    }

    /**
     * Calcula la media aritmética de un arreglo de valores.
     *
     * <pre>
     *   μ = (1/n) × Σ values[i],  para i = 0..n-1
     * </pre>
     *
     * <p>Complejidad: O(n) temporal, O(1) espacial.</p>
     *
     * @param values arreglo de valores numéricos
     * @return media aritmética, o 0.0 si el arreglo está vacío o es null
     */
    public static double calculateMean(double[] values) {
        if (values == null || values.length == 0) {
            return 0.0;
        }

        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }

        return sum / values.length;
    }

    /**
     * Calcula la desviación estándar muestral de un arreglo de valores usando
     * la corrección de Bessel (denominador n-1).
     *
     * <pre>
     *   σ = √( (1/(n-1)) × Σ (values[i] - μ)²,  para i = 0..n-1 )
     * </pre>
     *
     * <p><b>¿Por qué n-1 y no n?</b> La corrección de Bessel compensa el
     * sesgo que introduce el uso de la media muestral (μ̂) en lugar de la
     * media poblacional real. Al dividir por n-1 se obtiene un estimador
     * insesgado de la varianza poblacional σ².</p>
     *
     * <p>Complejidad: O(n) temporal, O(1) espacial.</p>
     *
     * @param values arreglo de valores numéricos (mínimo 2 elementos)
     * @param mean   media previamente calculada de los valores
     * @return desviación estándar muestral, o 0.0 si el arreglo tiene menos de 2 elementos
     */
    public static double calculateStandardDeviation(double[] values, double mean) {
        if (values == null || values.length < 2) {
            return 0.0;
        }

        double sumSquaredDiffs = 0.0;
        for (double value : values) {
            double diff = value - mean;
            sumSquaredDiffs += diff * diff;
        }

        // Corrección de Bessel: dividir por (n - 1) en lugar de n
        double variance = sumSquaredDiffs / (values.length - 1);

        return Math.sqrt(variance);
    }

    /**
     * Calcula la volatilidad histórica anualizada a partir de la desviación
     * estándar de los retornos diarios.
     *
     * <pre>
     *   σ_anual = σ_diaria × √252
     * </pre>
     *
     * <p>El factor √252 proviene del supuesto de independencia de retornos
     * diarios (i.i.d.) y la propiedad de escalamiento de la varianza bajo
     * dicho supuesto. Para procesos estocásticos con retornos i.i.d.:</p>
     * <pre>
     *   Var(R_T) = T × Var(R_1)  ⟹  σ(R_T) = √T × σ(R_1)
     * </pre>
     *
     * @param dailyStdDev desviación estándar de los retornos diarios
     * @return volatilidad anualizada como decimal (e.g., 0.35 = 35%)
     */
    public static double calculateAnnualizedVolatility(double dailyStdDev) {
        return dailyStdDev * Math.sqrt(TRADING_DAYS_PER_YEAR);
    }

    /**
     * Clasifica un activo financiero en una categoría de riesgo según
     * su volatilidad histórica anualizada.
     *
     * <h4>Criterios de clasificación (estándar de la industria):</h4>
     * <ul>
     *   <li><b>CONSERVADOR</b> (σ_anual &lt; 20%): Activos de baja volatilidad,
     *       típicamente bonos, utilities o empresas de gran capitalización estables.</li>
     *   <li><b>MODERADO</b> (20% ≤ σ_anual &lt; 40%): Activos de volatilidad media,
     *       la mayoría de acciones blue-chip y ETFs diversificados.</li>
     *   <li><b>AGRESIVO</b> (σ_anual ≥ 40%): Activos de alta volatilidad,
     *       acciones de mercados emergentes, commodities, empresas pequeñas.</li>
     * </ul>
     *
     * @param annualizedVolatility volatilidad histórica anualizada (como decimal)
     * @return cadena con la categoría de riesgo: "CONSERVADOR", "MODERADO" o "AGRESIVO"
     */
    public static String classifyRisk(double annualizedVolatility) {
        if (annualizedVolatility < CONSERVATIVE_THRESHOLD) {
            return "CONSERVADOR";
        } else if (annualizedVolatility < MODERATE_THRESHOLD) {
            return "MODERADO";
        } else {
            return "AGRESIVO";
        }
    }

    // =====================================================================
    // QUICKSORT PARA RANKING DE VOLATILIDAD
    // =====================================================================

    /**
     * Ordena un arreglo de {@link VolatilityResult} por volatilidad histórica
     * de menor a mayor usando el algoritmo QuickSort implementado manualmente.
     *
     * <h4>Descripción del algoritmo:</h4>
     * <p>QuickSort es un algoritmo de ordenamiento basado en la estrategia
     * "divide y vencerás" (divide and conquer). Selecciona un elemento pivote,
     * particiona el arreglo en dos sub-arreglos (elementos menores y mayores
     * al pivote) y aplica recursivamente el mismo proceso a cada partición.</p>
     *
     * <h4>Complejidad:</h4>
     * <ul>
     *   <li>Temporal: O(n log n) caso promedio, O(n²) peor caso</li>
     *   <li>Espacial: O(log n) por la pila de recursión</li>
     * </ul>
     *
     * <h4>Justificación de la elección:</h4>
     * <p>Se eligió QuickSort para mantener consistencia con los algoritmos de
     * ordenamiento ya implementados en el proyecto (Requerimiento 2), y por su
     * eficiencia práctica sobre arreglos de tamaño moderado (portafolio de ~20 activos).</p>
     *
     * @param results arreglo de resultados de volatilidad a ordenar in-place
     */
    public static void quickSortVolatility(VolatilityResult[] results) {
        if (results == null || results.length <= 1) {
            return;
        }
        quickSortVolatilityRecursive(results, 0, results.length - 1);
    }

    /**
     * Implementación recursiva del QuickSort para el arreglo de volatilidad.
     *
     * @param results arreglo a ordenar
     * @param low     índice inferior de la partición actual
     * @param high    índice superior de la partición actual
     */
    private static void quickSortVolatilityRecursive(VolatilityResult[] results, int low, int high) {
        if (low < high) {
            // Particionar y obtener el índice del pivote
            int pivotIndex = partitionVolatility(results, low, high);

            // Ordenar recursivamente las dos mitades
            quickSortVolatilityRecursive(results, low, pivotIndex - 1);
            quickSortVolatilityRecursive(results, pivotIndex + 1, high);
        }
    }

    /**
     * Realiza la partición del arreglo alrededor de un pivote (esquema de Lomuto).
     *
     * <p>Se utiliza el último elemento como pivote. Los elementos con volatilidad
     * menor al pivote se mueven a la izquierda; los mayores o iguales, a la derecha.</p>
     *
     * @param results arreglo a particionar
     * @param low     límite inferior
     * @param high    límite superior (elemento pivote)
     * @return índice final del pivote después de la partición
     */
    private static int partitionVolatility(VolatilityResult[] results, int low, int high) {
        // Pivote: volatilidad del último elemento
        double pivotVolatility = results[high].getHistoricalVolatility();

        // i marca la frontera entre elementos menores y mayores al pivote
        int i = low - 1;

        for (int j = low; j < high; j++) {
            // Si la volatilidad del elemento actual es menor que la del pivote,
            // moverlo a la partición izquierda
            if (results[j].getHistoricalVolatility() < pivotVolatility) {
                i++;
                // Intercambiar results[i] y results[j]
                VolatilityResult temp = results[i];
                results[i] = results[j];
                results[j] = temp;
            }
        }

        // Colocar el pivote en su posición final
        VolatilityResult temp = results[i + 1];
        results[i + 1] = results[high];
        results[high] = temp;

        return i + 1;
    }

    // =====================================================================
    // MÉTODOS AUXILIARES DE EXTRACCIÓN DE DATOS
    // =====================================================================

    /**
     * Extrae los precios de cierre de una lista de {@link PriceData} en un
     * arreglo primitivo de {@code double} para uso eficiente en los algoritmos.
     *
     * <p>La conversión a arreglo primitivo evita el overhead de autoboxing y
     * permite acceso directo por índice O(1) en los algoritmos de ventana
     * deslizante.</p>
     *
     * @param priceDataList lista de datos de precios ordenada cronológicamente
     * @return arreglo de precios de cierre
     */
    public static double[] extractClosePrices(List<PriceData> priceDataList) {
        double[] closePrices = new double[priceDataList.size()];
        for (int i = 0; i < priceDataList.size(); i++) {
            closePrices[i] = priceDataList.get(i).getClose();
        }
        return closePrices;
    }

    /**
     * Extrae los volúmenes de negociación de una lista de {@link PriceData}
     * en un arreglo primitivo de {@code long}.
     *
     * @param priceDataList lista de datos de precios ordenada cronológicamente
     * @return arreglo de volúmenes de negociación
     */
    public static long[] extractVolumes(List<PriceData> priceDataList) {
        long[] volumes = new long[priceDataList.size()];
        for (int i = 0; i < priceDataList.size(); i++) {
            volumes[i] = priceDataList.get(i).getVolume();
        }
        return volumes;
    }
}
