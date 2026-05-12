package com.example.demo.util;

/**
 * Clase utilitaria que contiene las implementaciones algorítmicas requeridas para
 * el soporte de las visualizaciones del Requerimiento 4.
 *
 * <p>Todas las implementaciones se realizan usando estructuras primitivas del lenguaje,
 * evitando librerías de alto nivel estadísticas o matemáticas.</p>
 */
public final class VisualizationAlgorithms {

    private VisualizationAlgorithms() {
        throw new UnsupportedOperationException("Clase utilitaria: no instanciar");
    }

    /**
     * Calcula la Media Móvil Simple (SMA) para un arreglo de precios.
     *
     * <p>El algoritmo utiliza una suma deslizante (sliding window sum) para
     * mantener una complejidad temporal de O(n), donde n es el número total de
     * precios. Esto es significativamente más eficiente que el acercamiento
     * ingenuo O(n * p), especialmente para series de tiempo largas.</p>
     *
     * @param prices arreglo cronológico de precios de cierre (close prices)
     * @param period período de la media móvil (ej. 20, 50, 200 días)
     * @return arreglo de objetos {@code Double} de la misma longitud que {@code prices}.
     *         Los primeros (period - 1) elementos serán nulos, pues no hay datos
     *         históricos suficientes para calcular la media.
     * @throws IllegalArgumentException si el período es menor a 1
     */
    public static Double[] calculateSMA(double[] prices, int period) {
        if (prices == null) {
            return new Double[0];
        }
        
        if (period < 1) {
            throw new IllegalArgumentException("El período de la SMA debe ser mayor o igual a 1");
        }

        int n = prices.length;
        Double[] sma = new Double[n];

        // Si hay menos datos que el período solicitado, todos los valores son nulos
        if (n < period) {
            return sma; // Retorna array lleno de nulos (comportamiento por defecto de Double[])
        }

        double windowSum = 0.0;

        // 1. Calcular la suma inicial de la primera ventana [0 ... period-1]
        for (int i = 0; i < period; i++) {
            windowSum += prices[i];
            
            // Todos los índices antes de completar el período quedan en null
            // excepto el último que ya puede tener una media
            if (i < period - 1) {
                sma[i] = null;
            }
        }

        // Registrar la media para el índice (period - 1)
        sma[period - 1] = windowSum / period;

        // 2. Deslizar la ventana por el resto del arreglo O(n)
        for (int i = period; i < n; i++) {
            // Añadir el nuevo precio que entra a la ventana
            // y restar el precio más viejo que sale de la ventana
            windowSum = windowSum + prices[i] - prices[i - period];
            
            // Calcular la media
            sma[i] = windowSum / period;
        }

        return sma;
    }
}
