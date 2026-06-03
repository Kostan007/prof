package com.example.agrimonitor.model;

import java.time.LocalDate;

public class HistoryEntry {
    private LocalDate date;
    private double avgNDVI;
    private double stressIndex;
    private String recommendations;

    public HistoryEntry(LocalDate date, double avgNDVI, double stressIndex, String recommendations) {
        this.date = date;
        this.avgNDVI = avgNDVI;
        this.stressIndex = stressIndex;
        this.recommendations = recommendations;
    }

    // ==================== GETTERS ====================
    public LocalDate getDate() { return date; }
    public double getAvgNDVI() { return avgNDVI; }
    public double getStressIndex() { return stressIndex; }
    public String getRecommendations() { return recommendations; }
}