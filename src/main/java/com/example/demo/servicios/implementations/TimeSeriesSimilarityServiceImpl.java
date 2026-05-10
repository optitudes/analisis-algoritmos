package com.example.demo.servicios.implementations;

import com.example.demo.dto.CosineSimilarityResultDTO;
import com.example.demo.dto.DTWResoultDTO;
import com.example.demo.dto.EuclideanDistancePointDTO;
import com.example.demo.dto.EuclideanDistanceResoultDTO;
import com.example.demo.dto.PearsonCorrelationResoultDTO;
import com.example.demo.dto.PriceDataProjection;
import com.example.demo.dto.WarpingStep;
import com.example.demo.servicios.interfaces.TimeSeriesSimilarityService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class TimeSeriesSimilarityServiceImpl implements TimeSeriesSimilarityService {

    public TimeSeriesSimilarityServiceImpl() {}

    @Override
    public PearsonCorrelationResoultDTO getPearsonCorrelation(List<PriceDataProjection> setX, List<PriceDataProjection> setY) {
        if (setX.size() != setY.size() || setX.isEmpty())
            throw new IllegalArgumentException("Las listas deben tener el mismo tamaño y no estar vacías");
        // Obtiene el inicio del cronometro
        long startTime = System.currentTimeMillis(); 
        int n = setX.size(); // Se toma un n, no importa cual ya que las listas son de igual tamanio
        // Variable que va a almacenar el promedio del setX
        double meanX = setX.stream() // Convierte la Lista a stream para procesar los datos mas rapidamente y funcionalmente
                .mapToDouble(p -> p.valueObtained().doubleValue()) // Obtiene el valor en decimal
                .average() // Obtiene el promedio de la lista de valores
                .orElseThrow(); // En caso de que falle, genera una excepcion
        // Variable que va a almacenar el promedio del setY
        double meanY = setY.stream() // Convierte la Lista a stream para procesar los datos mas rapidamente y funcionalmente 
                .mapToDouble(p -> p.valueObtained().doubleValue()) // Obtiiene el valor en decimal 
                .average() // Obtiene el promedio de la lista de valores
                .orElseThrow(); // En caso de que falle genera una excepcion
        /*
         * Inicializa las variables de las sumatorias en 0, deacuerdo a la formula 
         * 
         * coeficiente =  sxy/sx*sy
         */
        double sxy = 0, sx = 0, sy = 0;  
        // Itera simulando la sumatoria
        for (int i = 0; i < n; i++) {
            double dx = setX.get(i).valueObtained().doubleValue() - meanX;  // (xi - x̄)
            double dy = setY.get(i).valueObtained().doubleValue() - meanY;  // (yi - ȳ)

            sxy += dx * dy;   // Σ(xi - x̄)(yi - ȳ)
            sx  += dx * dx;   // Σ(xi - x̄)²
            sy  += dy * dy;   // Σ(yi - ȳ)²
        }
        double denominator = Math.sqrt(sx * sy);
        // Se obtiene el coeficiente de correlacion, en caso de  que el denominador sea 0 se asume que NO HAY correlacion
        double correlationCoeficient = denominator == 0 ? 0 : sxy / denominator; 
        // Obtiene el tiempo total de ejecucion en milisegundos
        long executionTime = System.currentTimeMillis() - startTime;
        // Obtiene la descripcion del resultado
        String description = getPearsonCorrelationDescription(correlationCoeficient);
        return new PearsonCorrelationResoultDTO(correlationCoeficient, description, executionTime);
    }

    /*
     * Dynamic Time Warping con ventana de Sakoe-Chiba.
     *
     * Complejidad: O(n * w) tiempo y espacio, donde w es el tamaño de la ventana.
     *
     * La ventana de Sakoe-Chiba restringe el camino a una banda diagonal:
     * solo se permiten celdas donde |i - j| <= windowSize
     *
     * @param windowSize ancho de la banda (recomendado: 10% del tamaño de la serie)
     */
    @Override
    public DTWResoultDTO getDTW(
            List<PriceDataProjection> setX,
            List<PriceDataProjection> setY
            ) {

        // Regla general: 20% del tamaño de la serie
        int windowSize = Math.max(1, (int) (setX.size() * 0.20));

        long startTime = System.currentTimeMillis();

        // Se obtiene las dimensiones de la matriz
        int n = setX.size();
        int m = setY.size();

        if (n == 0 || m == 0)
            throw new IllegalArgumentException("Las listas no pueden estar vacías");

        // Se obtiene una lista de los valores del setX
        double[] x = setX.stream()
                .mapToDouble(p -> p.valueObtained().doubleValue())
                .toArray();
        // Se obtiene una lista de los valores del setY
        double[] y = setY.stream()
                .mapToDouble(p -> p.valueObtained().doubleValue())
                .toArray();

        // Inicializa la matriz dynamic time warping
        double[][] dtw = new double[n + 1][m + 1];
        // Asigna a todas las celdas un costo infinito
        for (double[] row : dtw) Arrays.fill(row, Double.POSITIVE_INFINITY);
        // A la celda de inicio se le asigna un costo 0
        dtw[0][0] = 0.0;
        // Llenar la matriz respetando la ventana de Sakoe-Chiba ────────
        for (int i = 1; i <= n; i++) {
            // Banda de Sakoe-Chiba: solo procesa j dentro de la ventana
            int jStart = Math.max(1, i - windowSize);
            int jEnd   = Math.min(m, i + windowSize);

            for (int j = jStart; j <= jEnd; j++) {
                double cost = Math.abs(x[i - 1] - y[j - 1]); // distancia local

                double best = Math.min(
                        dtw[i - 1][j],      // viene de arriba
                        Math.min(
                                dtw[i][j - 1],      // viene de la izquierda
                                dtw[i - 1][j - 1]   // viene de la diagonal
                        )
                );

                dtw[i][j] = cost + best;
            }
        }

        double totalCost = dtw[n][m];

        // Lista de pasos del camino óptimo
        List<WarpingStep> path = new ArrayList<>();

        // Se inicia desde el final de la matriz (ultimo elemento o meta)
        int i = n, j = m;

        // Se recorre la matriz hasta llegar al punto de partida
        while (i > 0 && j > 0) {
            // Se obtiene la fecha del elemento en x
            LocalDate dateX = setX.get(i - 1).date();
            // Se obtiene la fecha del elemento en y
            LocalDate dateY = setY.get(j - 1).date();
            // Se obtiene el costo de la celda actual
            double stepCost = Math.abs(x[i - 1] - y[j - 1]);
            // Se agregga la celda actual al path optimo
            path.add(new WarpingStep(dateX, dateY, stepCost));

            // Se obtiene los costos de los movimientos
            double diagonal = dtw[i - 1][j - 1]; // Se obtiene el costo de moverse diagonalmente 
            double up       = dtw[i - 1][j]; // Se obtiene el costo de moverse hacia arriba
            double left     = dtw[i][j - 1]; // se obtiene el costo de moverse hacia la izquierda

            // Valida si el costo de moverse en diagonal es menor o igual
            // al costo de moverse hacia arriba y hacia la izquierda
            if (diagonal <= up && diagonal <= left) {
                i--; j--;  // se mueve hacia la diagonal
            } else if (up <= left) { // Valida el caso en que el costo de moverse hacia arriba sea menor al de moverse a la izquierda
                i--;       // se mueve hacia arriba
            } else {  
                j--;       // se mueve hacia la izquierda
            }
        }

        // El backtracking construye el camino al revés (del final al inicio)
        // por lo que se usa collections.reverse para ordenar de inicio a fin
        Collections.reverse(path);

        long executionTime = System.currentTimeMillis() - startTime;

        return new DTWResoultDTO(totalCost, path, executionTime, dtw);
    }

    /*
     * Calcula la distancia euclidiana por fecha entre dos series de tiempo.
     * Ambas listas deben estar ordenadas y tener las mismas fechas.
     *
     * distance(i) = √((xi - yi)²) = |xi - yi|
     */
    @Override
    public EuclideanDistanceResoultDTO getEuclideanDistanceList(
            List<PriceDataProjection> setX,
            List<PriceDataProjection> setY) {

        // Valida que los tamanios de los sets de datos sean correctos
        if (setX.size() != setY.size() || setX.isEmpty())
            throw new IllegalArgumentException("Las listas deben tener el mismo tamaño y no estar vacías");

        // Obtiene el inicio del cronometro
        long startTime = System.currentTimeMillis();

        // Crea una lista de puntos de distancia euclideana
        List<EuclideanDistancePointDTO> distanceList = new ArrayList<EuclideanDistancePointDTO>();

        // Recorre todos los puntos de ambos sets de datos (tienen el mismo tamanio)
        for (int i = 0; i < setX.size(); i++) {
            // Obtiene la fecha del punto del set X
            LocalDate dateX = setX.get(i).date();
            // Obtiene la fecha del punto del set Y
            LocalDate dateY = setY.get(i).date();

            // Se asegura de que las fechas sean iguales
            if (!dateX.equals(dateY))
                throw new IllegalArgumentException(
                    "Las fechas no coinciden en el índice " + i + ": " + dateX + " vs " + dateY
                );

            // Obtiene el valor en xi
            double xi = setX.get(i).valueObtained().doubleValue();
            // Obtiene el valor en yi
            double yi = setY.get(i).valueObtained().doubleValue();
            // Calcula la distancia euclideana entre ambos puntos asumiendo que x2 y y2 son iguales por tanto (0-0)^2 = 0
            double distance = Math.round(Math.sqrt(Math.pow(xi - yi, 2)) * 1000.0) / 1000.0; // √((xi - yi)²)
            // Agrega la distancia euclideadana a la lista de distancias euclideanas
            distanceList.add(new EuclideanDistancePointDTO(dateX, distance));
        }
        // Obtiene el tiempo total de ejecucion en milisegundos
        long executionTime = System.currentTimeMillis() - startTime;
        return new EuclideanDistanceResoultDTO(distanceList, executionTime);
    }

    /*
     * Genera la descripción textual del coeficiente de correlación de Pearson.
     * Rango válido: [-1, 1]
     *
     *  c < 0.1  → Sin correlación
     *  c < 0.3  → Baja correlación
     *  c < 0.5  → Media correlación
     *  c < 0.7  → Alta correlación
     *  c <= 1.0 → Correlación muy alta
     */
    public String getPearsonCorrelationDescription(double c) {
        if (c < -1 || c > 1)
            throw new IllegalStateException("El coeficiente de correlación está fuera del rango válido [-1, 1]: " + c);

        String sign = c >= 0 ? "positiva" : "negativa";
        double abs  = Math.abs(c);

        if (abs < 0.1) return "Sin correlación";
        if (abs < 0.3) return "Baja correlación " + sign;
        if (abs < 0.5) return "Media correlación " + sign;
        if (abs < 0.7) return "Alta correlación " + sign;
        return "Correlación muy alta " + sign;
    }

    /**
     * Calcula la similitud coseno tratando cada serie completa como un vector.
     *
     *         Σ(xi * yi)
     * cos = ──────────────────────────
     *       √Σ(xi²)  *  √Σ(yi²)
     *
     * Resultado en [-1, 1]:
     *   1  → series perfectamente proporcionales (misma tendencia)
     *   0  → sin relación lineal
     *  -1  → tendencias completamente opuestas
     */
    @Override
    public CosineSimilarityResultDTO getCosineSimilarity(
            List<PriceDataProjection> setX,
            List<PriceDataProjection> setY) {

        if (setX.size() != setY.size() || setX.isEmpty())
            throw new IllegalArgumentException("Las listas deben tener el mismo tamaño y no estar vacías");

        long startTime = System.currentTimeMillis();

        double dotProduct = 0;   // Σ(xi * yi)
        double normX      = 0;   // Σ(xi²)
        double normY      = 0;   // Σ(yi²)

        for (int i = 0; i < setX.size(); i++) {
            LocalDate dateX = setX.get(i).date();
            LocalDate dateY = setY.get(i).date();

            if (!dateX.equals(dateY))
                throw new IllegalArgumentException(
                    "Las fechas no coinciden en el índice " + i + ": " + dateX + " vs " + dateY
                );

            double xi = setX.get(i).valueObtained().doubleValue();
            double yi = setY.get(i).valueObtained().doubleValue();

            dotProduct += xi * yi;   // producto punto acumulado
            normX      += xi * xi;   // magnitud X acumulada
            normY      += yi * yi;   // magnitud Y acumulada
        }

        double denominator = Math.sqrt(normX) * Math.sqrt(normY);

        // Si alguna serie es todo ceros, la similitud es indefinida → se asume 0
        double similarity = denominator == 0 ? 0 : dotProduct / denominator;

        // Redondeo a 3 decimales
        similarity = Math.round(similarity * 1000.0) / 1000.0;

        long executionTime = System.currentTimeMillis() - startTime;

        String description = buildCosineDescription(similarity); 

        return new CosineSimilarityResultDTO(similarity, description, executionTime);
    }

    private String buildCosineDescription(double c) {
        if (c < -1 || c > 1) return "Similitud fuera de rango [-1, 1]: " + c;
        if (Math.abs(c) < 0.1) return "Sin similitud direccional";
        if (Math.abs(c) < 0.3) return "Baja similitud " + (c > 0 ? "positiva" : "negativa");
        if (Math.abs(c) < 0.5) return "Media similitud " + (c > 0 ? "positiva" : "negativa");
        if (Math.abs(c) < 0.7) return "Alta similitud " + (c > 0 ? "positiva" : "negativa");
        return "Similitud muy alta " + (c > 0 ? "positiva" : "negativa");
    }
}
