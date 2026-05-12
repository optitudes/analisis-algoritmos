package com.example.demo.servicios.interfaces;

import com.example.demo.dto.CandlestickResponseDTO;

/**
 * Interfaz de servicio para las visualizaciones requeridas en el Requerimiento 4.
 */
public interface VisualizationService {

    /**
     * Obtiene los datos necesarios para renderizar un gráfico de velas (candlestick)
     * de un activo financiero, incluyendo el cálculo algorítmico de su
     * Media Móvil Simple (SMA).
     *
     * @param activeId el ID del activo a consultar
     * @param smaPeriod el período (en días) para el cálculo de la Media Móvil Simple
     * @return un DTO que envuelve los metadatos del activo y la serie de tiempo con precios y SMA.
     *         Retorna null si el activo no existe.
     */
    CandlestickResponseDTO getCandlestickData(Long activeId, int smaPeriod);
}
