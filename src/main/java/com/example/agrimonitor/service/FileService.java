package com.example.agrimonitor.service;

import com.example.agrimonitor.model.Field;
import com.example.agrimonitor.model.HistoryEntry;
import com.example.agrimonitor.model.Zone;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.layout.GridPane;

public class FileService {

    private static final String DATA_DIR = "src/main/resources/com/example/agrimonitor/data/";

    public FileService() {
        createDataDirectory();
    }

    private void createDataDirectory() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Не удалось создать папку data: " + e.getMessage());
        }
    }

    public void saveField(Field field) {
        String filename = DATA_DIR + "fields.csv";
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename, true))) {
            pw.println(String.join(",",
                    field.getName(),
                    field.getCrop(),
                    field.getGrowthPhase(),
                    String.valueOf(field.getArea()),
                    String.valueOf(field.getGridSize())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Field> loadAllFields() {
        List<Field> fields = new ArrayList<>();
        File file = new File(DATA_DIR + "fields.csv");
        if (!file.exists()) return fields;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",");
                if (p.length >= 5) {
                    fields.add(new Field(p[0], p[1], p[2],
                            Double.parseDouble(p[3]),
                            50, 50,
                            Integer.parseInt(p[4])));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fields;
    }

    public void saveFieldData(Field field) {
        String filename = DATA_DIR + "zones_" + safe(field.getName()) + ".csv";
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("Zone,NDVI,SoilTemp,SoilMoisture,Stress,Status");
            for (Zone z : field.getZones()) {
                pw.printf("%s,%.3f,%.1f,%.1f,%.1f,%s%n",
                        z.getZoneName(), z.getNdvi(), z.getSoilTemp(),
                        z.getSoilMoisture(), z.getStressIndex(), z.getStatus());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteField(Field field) {
        List<Field> all = loadAllFields();
        all.removeIf(f -> f.getName().equals(field.getName()));
        saveAllFields(all);
    }

    public void saveAllFields(List<Field> fields) {
        String filename = DATA_DIR + "fields.csv";
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename, false))) {
            for (Field f : fields) {
                pw.println(String.join(",",
                        f.getName(), f.getCrop(), f.getGrowthPhase(),
                        String.valueOf(f.getArea()), String.valueOf(f.getGridSize())));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Сохранить историю съёмок в data/history_<name>.csv */
    public void saveHistory(Field field) {
        String filename = DATA_DIR + "history_" + safe(field.getName()) + ".csv";
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename, false))) {
            pw.println("Date|AvgNDVI|AvgStress|WeatherData|Recommendations|ZoneNDVIs");
            for (HistoryEntry e : field.getHistory()) {
                String zoneNDVIs = "";
                if (e.getZoneSnapshots() != null && !e.getZoneSnapshots().isEmpty()) {
                    zoneNDVIs = e.getZoneSnapshots().stream()
                            .map(z -> String.format("%.4f", z.getNdvi()))
                            .collect(Collectors.joining(";"));
                }
                String recs = e.getRecommendations()
                        .replace("|", " ").replace("\n", " ").replace("\r", "");
                String weather = e.getWeatherData().replace("|", " ");
                pw.printf("%s|%.4f|%.2f|%s|%s|%s%n",
                        e.getDate(), e.getAvgNDVI(), e.getStressIndex(),
                        weather, recs, zoneNDVIs);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Загрузить историю съёмок из data/history_<name>.csv */
    public List<HistoryEntry> loadHistory(Field field) {
        List<HistoryEntry> history = new ArrayList<>();
        File file = new File(DATA_DIR + "history_" + safe(field.getName()) + ".csv");
        if (!file.exists()) return history;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split("\\|", 6);
                if (p.length < 5) continue;
                try {
                    LocalDate date = LocalDate.parse(p[0].trim());
                    double avgNDVI = Double.parseDouble(p[1].trim());
                    double stress  = Double.parseDouble(p[2].trim());
                    String weather = p[3].trim();
                    String recs    = p[4].trim();

                    List<Zone> zones = new ArrayList<>();
                    if (p.length >= 6 && !p[5].trim().isEmpty()) {
                        String[] ndvis = p[5].trim().split(";");
                        int gs = field.getGridSize();
                        for (int i = 0; i < ndvis.length; i++) {
                            Zone z = new Zone(i / gs + 1, i % gs + 1);
                            z.setNdvi(Double.parseDouble(ndvis[i]));
                            zones.add(z);
                        }
                    }
                    history.add(new HistoryEntry(date, avgNDVI, stress, weather, recs, zones));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return history;
    }

    public void exportReport(Field field) {
        String filename = DATA_DIR + "report_" + safe(field.getName()) + ".txt";
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("=== ОТЧЁТ ПО ПОЛЮ: " + field.getName() + " ===");
            pw.println("Культура: " + field.getCrop());
            pw.println("Фаза: " + field.getGrowthPhase());
            pw.println("Площадь: " + field.getArea() + " га");
            pw.println("Дата: " + LocalDate.now());
            pw.println("Средний NDVI: " + String.format("%.3f", field.getAverageNDVI()));
            pw.println("Средний стресс: " + String.format("%.1f", field.getAverageStress()) + "%");
            pw.println("\n=== ЗОНЫ ===");
            for (Zone z : field.getZones()) {
                pw.printf("Зона %s | NDVI: %.3f | Стресс: %.1f%% | %s%n",
                        z.getZoneName(), z.getNdvi(), z.getStressIndex(), z.getStatus());
            }
            System.out.println("Отчёт сохранён: " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Заглушка — PNG export */
    public void exportHeatMapAsPNG(GridPane grid, String fieldName) {
        System.out.println("PNG экспорт (заглушка): " + DATA_DIR + "map_" + safe(fieldName) + ".png");
    }

    private String safe(String name) {
        return name.replace(" ", "_").replaceAll("[^a-zA-Zа-яА-Я0-9_]", "");
    }
}
