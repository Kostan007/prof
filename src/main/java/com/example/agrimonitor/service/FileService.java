package com.example.agrimonitor.service;

import com.example.agrimonitor.model.Field;
import com.example.agrimonitor.model.Zone;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
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
        // Пока сохраняем только базовую информацию
        String filename = DATA_DIR + "fields.csv";
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename, true))) {
            pw.println(String.join(",",
                    field.getName(),
                    field.getCrop(),
                    field.getGrowthPhase(),
                    String.valueOf(field.getArea()),
                    String.valueOf(field.getGridSize())
            ));
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
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    Field f = new Field(
                            parts[0],
                            parts[1],
                            parts[2],
                            Double.parseDouble(parts[3]),
                            50, 50, // center X,Y
                            Integer.parseInt(parts[4])
                    );
                    fields.add(f);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fields;
    }

    public void saveFieldData(Field field) {
        String filename = DATA_DIR + "zones_" + field.getName().replace(" ", "_") + ".csv";

        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("Zone,NDVI,SoilTemp,SoilMoisture,Stress,Status");

            for (Zone z : field.getZones()) {
                pw.printf("%s,%.3f,%.1f,%.1f,%.1f,%s%n",
                        z.getZoneName(),
                        z.getNdvi(),
                        z.getSoilTemp(),
                        z.getSoilMoisture(),
                        z.getStressIndex(),
                        z.getStatus());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void deleteField(Field field) {
        List<Field> allFields = loadAllFields();

        allFields.removeIf(f -> f.getName().equals(field.getName()));

        // перезаписываем файл
        saveAllFields(allFields);
    }
    public void saveAllFields(List<Field> fields) {
        try (PrintWriter writer = new PrintWriter("fields.csv")) {
            for (Field f : fields) {
                writer.println(f.toCSV());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Экспорт текстового отчёта
     */
    public void exportReport(Field field) {
        String filename = DATA_DIR + "report_" + field.getName().replace(" ", "_") + ".txt";
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

    /**
     * Экспорт тепловой карты (упрощённо)
     */
    public void exportHeatMapAsPNG(GridPane grid, String fieldName) {
        String filename = DATA_DIR + "map_" + fieldName.replace(" ", "_") + ".png";
        System.out.println("Карта сохранена (имитация): " + filename);
        // Полноценный добавить позже
    }

}