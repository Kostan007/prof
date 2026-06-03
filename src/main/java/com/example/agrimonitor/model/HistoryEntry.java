package com.example.agrimonitor.model;

import java.time.LocalDate;
import java.util.List;

public class HistoryEntry {
    private LocalDate date;
    private double avgNDVI;
    private double stressIndex;
    private String weatherData;
    private String recommendations;
    private List<Zone> zoneSnapshots;

    public HistoryEntry(LocalDate date, double avgNDVI, double stressIndex,
                        String weatherData, String recommendations, List<Zone> zoneSnapshots) {
        this.date = date;
        this.avgNDVI = avgNDVI;
        this.stressIndex = stressIndex;
        this.weatherData = weatherData != null ? weatherData : "";
        this.recommendations = recommendations;
        this.zoneSnapshots = zoneSnapshots;
    }

    public LocalDate getDate() { return date; }
    public double getAvgNDVI() { return avgNDVI; }
    public double getStressIndex() { return stressIndex; }
    public String getWeatherData() { return weatherData; }
    public String getRecommendations() { return recommendations; }
    public List<Zone> getZoneSnapshots() { return zoneSnapshots; }
}
