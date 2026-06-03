package com.example.agrimonitor.model;

import java.util.Random;

public class Zone {
    private int row;
    private int col;
    private double ndvi = 0.5;
    private double soilTemp = 22.0;
    private double soilMoisture = 60.0;
    private double stressIndex = 0.0;
    private String status = "Не определено";

    public Zone(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public String getZoneName() {
        return "(" + row + "," + col + ")";
    }

    public void generateRandomNDVI() {
        Random rand = new Random();
        this.ndvi = -0.2 + rand.nextDouble() * 1.1; // от -0.2 до 0.9
        if (this.ndvi > 1.0) this.ndvi = 1.0;
    }

    // ==================== GETTERS & SETTERS ====================

    public int getRow() { return row; }
    public int getCol() { return col; }

    public double getNdvi() { return ndvi; }
    public void setNdvi(double ndvi) {
        if (ndvi < -1.0 || ndvi > 1.0) {
            this.ndvi = 0.0;
        } else {
            this.ndvi = ndvi;
        }
    }

    public double getSoilTemp() { return soilTemp; }
    public void setSoilTemp(double soilTemp) { this.soilTemp = soilTemp; }

    public double getSoilMoisture() { return soilMoisture; }
    public void setSoilMoisture(double soilMoisture) { this.soilMoisture = soilMoisture; }

    public double getStressIndex() { return stressIndex; }
    public void setStressIndex(double stressIndex) {
        this.stressIndex = Math.max(0, Math.min(100, stressIndex));
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}