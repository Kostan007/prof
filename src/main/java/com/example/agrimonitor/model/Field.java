package com.example.agrimonitor.model;

import com.example.agrimonitor.service.StressCalculator;   // ← ЭТО БЫЛО НУЖНО!

import java.util.ArrayList;
import java.util.List;

public class Field {
    private String name;
    private String crop;
    private String growthPhase;
    private double area;
    private double centerX;
    private double centerY;
    private int gridSize;
    private List<Zone> zones = new ArrayList<>();
    private List<HistoryEntry> history = new ArrayList<>();
    private int width;
    private int height;

    public Field(String name, String crop, String growthPhase, double area,
                 double centerX, double centerY, int gridSize) {
        this.name = name;
        this.crop = crop != null ? crop : "Пшеница";
        this.growthPhase = growthPhase != null ? growthPhase : "Вегетация";
        this.area = area;
        this.centerX = centerX;
        this.centerY = centerY;
        this.gridSize = gridSize;
        initZones();
    }

    private void initZones() {
        zones.clear();
        for (int i = 1; i <= gridSize; i++) {
            for (int j = 1; j <= gridSize; j++) {
                zones.add(new Zone(i, j));
            }
        }
    }

    /** Главный метод расчёта стресса */
    public void calculateAllStress(StressCalculator calculator) {
        for (Zone zone : zones) {
            calculator.calculateStress(zone);
        }
    }

    public double getAverageNDVI() {
        return zones.stream().mapToDouble(Zone::getNdvi).average().orElse(0.0);
    }

    public double getAverageStress() {
        return zones.stream().mapToDouble(Zone::getStressIndex).average().orElse(0.0);
    }
    public String toCSV() {
        return name + "," + width + "," + height;
    }

    // ==================== GETTERS & SETTERS ====================

    public String getName() { return name; }
    public String getCrop() { return crop; }
    public String getGrowthPhase() { return growthPhase; }
    public double getArea() { return area; }
    public int getGridSize() { return gridSize; }
    public List<Zone> getZones() { return zones; }
    public List<HistoryEntry> getHistory() { return history; }

    @Override
    public String toString() {
        return name + " (" + crop + ")";
    }
}