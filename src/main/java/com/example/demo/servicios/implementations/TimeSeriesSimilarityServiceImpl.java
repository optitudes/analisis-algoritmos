package com.example.demo.servicios.implementations;

import com.example.demo.dto.EuclideanDistancePointDTO;
import com.example.demo.dto.EuclideanDistanceResoultDTO;
import com.example.demo.dto.PearsonCorrelationResoultDTO;
import com.example.demo.dto.PriceDataProjection;
import com.example.demo.servicios.interfaces.TimeSeriesSimilarityService;

import java.time.LocalDate;
import java.util.ArrayList;
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
}
