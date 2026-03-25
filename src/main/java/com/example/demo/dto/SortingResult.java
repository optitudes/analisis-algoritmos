package com.example.demo.dto;

public class SortingResult {
    private String algorithmName;
    private String bigOComplexity;
    private int size;
    private double timeInMilliseconds;

    public SortingResult() {
    }

    public SortingResult(String algorithmName, String bigOComplexity, int size, double timeInMilliseconds) {
        this.algorithmName = algorithmName;
        this.bigOComplexity = bigOComplexity;
        this.size = size;
        this.timeInMilliseconds = timeInMilliseconds;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public String getBigOComplexity() {
        return bigOComplexity;
    }

    public void setBigOComplexity(String bigOComplexity) {
        this.bigOComplexity = bigOComplexity;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public double getTimeInMilliseconds() {
        return timeInMilliseconds;
    }

    public void setTimeInMilliseconds(double timeInMilliseconds) {
        this.timeInMilliseconds = timeInMilliseconds;
    }
}
