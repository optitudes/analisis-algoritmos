package com.example.demo.dto;

/**
 * DTO que representa un punto de datos individual para el gráfico de velas
 * (candlestick chart) requerido en el Requerimiento 4.
 *
 * Incluye los precios estándar OHLC (Open, High, Low, Close), el volumen y
 * el valor calculado algorítmicamente de la Media Móvil Simple (SMA).
 */
public class CandlestickDataDTO {

    private String date;
    private double open;
    private double high;
    private double low;
    private double close;
    private long volume;
    
    /**
     * Valor de la Media Móvil Simple para esta fecha.
     * Es un objeto Double (en lugar de primitivo) para permitir valores nulos,
     * ya que la SMA no se puede calcular en los primeros días antes de que se
     * cumpla el período configurado.
     */
    private Double sma;

    public CandlestickDataDTO() {
    }

    public CandlestickDataDTO(String date, double open, double high, double low, double close, long volume, Double sma) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.sma = sma;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public long getVolume() {
        return volume;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }

    public Double getSma() {
        return sma;
    }

    public void setSma(Double sma) {
        this.sma = sma;
    }
}
