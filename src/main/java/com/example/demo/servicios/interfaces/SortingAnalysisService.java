package com.example.demo.servicios.interfaces;

import com.example.demo.dto.SortingResult;
import com.example.demo.entidades.PriceData;
import java.util.List;

public interface SortingAnalysisService {
    /**
     * Ejecuta los 12 algoritmos sobre los registros de la DB.
     * @param limit Indica un tamaño máximo. Si es <= 0 usa todos los registros.
     * @return Resultados con los tiempos.
     */
    List<SortingResult> runAnalysis(int limit);

    /**
     * Obtiene los 15 registros (días) con mayor volumen y los retorna
     * ordenados ascendentemente (por fecha/cierre).
     */
    List<PriceData> getTop15DaysByVolume();
}
