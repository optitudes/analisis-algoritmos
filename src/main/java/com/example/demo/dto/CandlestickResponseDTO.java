package com.example.demo.dto;

import java.util.List;

/**
 * DTO que envuelve la respuesta completa para el gráfico de velas del Requerimiento 4.
 *
 * Contiene metadatos del activo analizado, el parámetro de período utilizado para
 * la media móvil (SMA), y la lista completa de puntos de datos listos para
 * ser consumidos por una librería de gráficos en el frontend.
 */
public class CandlestickResponseDTO {

    private Long activeId;
    private String activeSymbol;
    private String activeName;
    private int smaPeriod;
    private List<CandlestickDataDTO> data;

    public CandlestickResponseDTO() {
    }

    public CandlestickResponseDTO(Long activeId, String activeSymbol, String activeName, int smaPeriod, List<CandlestickDataDTO> data) {
        this.activeId = activeId;
        this.activeSymbol = activeSymbol;
        this.activeName = activeName;
        this.smaPeriod = smaPeriod;
        this.data = data;
    }

    public Long getActiveId() {
        return activeId;
    }

    public void setActiveId(Long activeId) {
        this.activeId = activeId;
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

    public int getSmaPeriod() {
        return smaPeriod;
    }

    public void setSmaPeriod(int smaPeriod) {
        this.smaPeriod = smaPeriod;
    }

    public List<CandlestickDataDTO> getData() {
        return data;
    }

    public void setData(List<CandlestickDataDTO> data) {
        this.data = data;
    }
}
