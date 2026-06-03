package com.example.agrimonitor.controller;

import com.example.agrimonitor.model.Field;
import com.example.agrimonitor.model.HistoryEntry;
import com.example.agrimonitor.model.Zone;
import com.example.agrimonitor.service.FileService;
import com.example.agrimonitor.service.StressCalculator;
import com.example.agrimonitor.util.ColorUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    // === Поля ===
    @FXML private TextField fieldNameField;
    @FXML private ComboBox<String> cropComboBox;
    @FXML private ComboBox<String> phaseComboBox;
    @FXML private Spinner<Double>  areaSpinner;
    @FXML private Spinner<Double>  centerXSpinner;
    @FXML private Spinner<Double>  centerYSpinner;
    @FXML private Spinner<Integer> gridSizeSpinner;
    @FXML private ListView<Field>  fieldsListView;

    // === Мониторинг — метеоданные ===
    @FXML private Spinner<Double> airTempSpinner;
    @FXML private Spinner<Double> precipitationSpinner;
    @FXML private Spinner<Double> airHumiditySpinner;
    @FXML private Spinner<Double> radiationSpinner;
    @FXML private DatePicker      surveyDatePicker;

    // === Мониторинг — таблица зон ===
    @FXML private TableView<Zone>          zonesTableView;
    @FXML private TableColumn<Zone, String> zoneColumn;
    @FXML private TableColumn<Zone, Double> ndviColumn;
    @FXML private TableColumn<Zone, Double> soilTempColumn;
    @FXML private TableColumn<Zone, Double> soilMoistureColumn;
    @FXML private TableColumn<Zone, Double> stressColumn;
    @FXML private TableColumn<Zone, String> statusColumn;

    // === Мониторинг — карта и статистика ===
    @FXML private GridPane  heatMapGrid;
    @FXML private Label     avgNDVILabel;
    @FXML private Label     stressIndexLabel;
    @FXML private Label     healthyZonesLabel;
    @FXML private Label     criticalZonesLabel;
    @FXML private TextArea  recommendationsArea;
    @FXML private BarChart<String, Number> stressBarChart;

    // === Прогноз ===
    @FXML private TextField forecastTempsField;
    @FXML private TextField forecastRainsField;
    @FXML private TextArea  forecastArea;

    // === История ===
    @FXML private TableView<HistoryEntry>          historyTableView;
    @FXML private TableColumn<HistoryEntry, ?>     historyDateColumn;
    @FXML private TableColumn<HistoryEntry, Double> historyNDVIColumn;
    @FXML private TableColumn<HistoryEntry, Double> historyStressColumn;
    @FXML private TableColumn<HistoryEntry, String> historyWeatherColumn;
    @FXML private TableColumn<HistoryEntry, String> historyRecColumn;
    @FXML private LineChart<String, Number> ndviLineChart;
    @FXML private GridPane compareMapGrid;

    // === State ===
    private final ObservableList<Field> fields = FXCollections.observableArrayList();
    private Field currentField = null;
    private final StressCalculator calculator = new StressCalculator();
    private final FileService fileService = new FileService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupSpinners();
        setupComboBoxes();
        setupZonesTable();
        setupHistoryTable();

        surveyDatePicker.setValue(LocalDate.now());

        fieldsListView.setItems(fields);
        fieldsListView.getSelectionModel().selectedItemProperty().addListener((obs, old, newField) -> {
            currentField = newField;
            if (newField != null) showCurrentField();
        });

        fields.addAll(fileService.loadAllFields());
    }

    // ==================== INIT ====================

    private void setupSpinners() {
        areaSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1, 1000, 50, 0.5));
        centerXSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100, 50, 1));
        centerYSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100, 50, 1));
        gridSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 8, 4));

        airTempSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-10, 40, 22, 0.5));
        precipitationSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 200, 15, 1));
        airHumiditySpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(20, 100, 65, 1));
        radiationSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 8, 4, 0.1));
    }

    private void setupComboBoxes() {
        cropComboBox.getItems().addAll("Пшеница", "Кукуруза", "Подсолнечник", "Соя", "Томаты", "Огурцы");
        phaseComboBox.getItems().addAll("Всходы", "Вегетация", "Цветение", "Созревание");
        cropComboBox.setValue("Пшеница");
        phaseComboBox.setValue("Вегетация");
    }

    private void setupZonesTable() {
        zoneColumn.setCellValueFactory(new PropertyValueFactory<>("zoneName"));
        ndviColumn.setCellValueFactory(new PropertyValueFactory<>("ndvi"));
        soilTempColumn.setCellValueFactory(new PropertyValueFactory<>("soilTemp"));
        soilMoistureColumn.setCellValueFactory(new PropertyValueFactory<>("soilMoisture"));
        stressColumn.setCellValueFactory(new PropertyValueFactory<>("stressIndex"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    @SuppressWarnings("unchecked")
    private void setupHistoryTable() {
        ((TableColumn<HistoryEntry, LocalDate>) historyDateColumn)
                .setCellValueFactory(new PropertyValueFactory<>("date"));
        historyNDVIColumn.setCellValueFactory(new PropertyValueFactory<>("avgNDVI"));
        historyStressColumn.setCellValueFactory(new PropertyValueFactory<>("stressIndex"));
        historyWeatherColumn.setCellValueFactory(new PropertyValueFactory<>("weatherData"));
        historyRecColumn.setCellValueFactory(new PropertyValueFactory<>("recommendations"));

        historyNDVIColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%.3f", v));
            }
        });
        historyStressColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%.1f%%", v));
            }
        });
    }

    // ==================== ПОЛЯ ====================

    @FXML
    private void addField() {
        String name = fieldNameField.getText().trim();
        if (name.isEmpty()) {
            showAlert("Ошибка", "Введите название поля!", Alert.AlertType.ERROR);
            return;
        }
        Field field = new Field(name, cropComboBox.getValue(), phaseComboBox.getValue(),
                areaSpinner.getValue(), centerXSpinner.getValue(), centerYSpinner.getValue(),
                gridSizeSpinner.getValue());
        fields.add(field);
        fileService.saveField(field);
        fieldsListView.getSelectionModel().select(field);
    }

    @FXML
    private void deleteField() {
        Field selected = fieldsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Ошибка", "Выберите поле для удаления!", Alert.AlertType.WARNING);
            return;
        }
        fields.remove(selected);
        if (selected == currentField) {
            currentField = null;
            zonesTableView.getItems().clear();
            heatMapGrid.getChildren().clear();
        }
        fileService.deleteField(selected);
        showAlert("Успешно", "Поле удалено", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void resetForm() {
        fieldNameField.clear();
        cropComboBox.setValue("Пшеница");
        phaseComboBox.setValue("Вегетация");
        areaSpinner.getValueFactory().setValue(50.0);
        centerXSpinner.getValueFactory().setValue(50.0);
        centerYSpinner.getValueFactory().setValue(50.0);
        gridSizeSpinner.getValueFactory().setValue(4);
    }

    // ==================== МОНИТОРИНГ ====================

    @FXML
    private void calculateStress() {
        if (currentField == null) return;
        double airTemp  = airTempSpinner.getValue();
        double rain     = precipitationSpinner.getValue();
        double humidity = airHumiditySpinner.getValue();
        double radiation = radiationSpinner.getValue();
        currentField.calculateAllStress(calculator, airTemp, rain, humidity, radiation);
        showCurrentField();
        fileService.saveFieldData(currentField);
    }

    @FXML
    private void applyWeatherToAll() {
        if (currentField == null) {
            showAlert("Ошибка", "Сначала выберите поле!", Alert.AlertType.WARNING);
            return;
        }
        double airTemp = airTempSpinner.getValue();
        double precip  = precipitationSpinner.getValue();
        double radiation = radiationSpinner.getValue();
        for (Zone zone : currentField.getZones()) {
            zone.setSoilTemp(airTemp + (Math.random() * 4 - 2));
            zone.setSoilMoisture(Math.max(10, Math.min(90, 50 + (precip * 0.8) - (airTemp * 0.5))));
            zone.setSolarRadiation(radiation);
        }
        calculateStress();
    }

    @FXML
    private void generateRandomNDVI() {
        if (currentField == null) return;
        currentField.getZones().forEach(Zone::generateRandomNDVI);
        calculateStress();
    }

    @FXML
    private void loadNDVIFromCSV() {
        if (currentField == null) {
            showAlert("Ошибка", "Сначала выберите поле!", Alert.AlertType.WARNING);
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Загрузить NDVI из CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        File file = fc.showOpenDialog(null);
        if (file == null) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            List<Zone> zones = currentField.getZones();
            int idx = 0;
            String line;
            while ((line = br.readLine()) != null && idx < zones.size()) {
                for (String val : line.trim().split("[,;\\s]+")) {
                    if (idx >= zones.size()) break;
                    try {
                        zones.get(idx).setNdvi(Double.parseDouble(val.trim()));
                        idx++;
                    } catch (NumberFormatException ignored) {}
                }
            }
            calculateStress();
            showAlert("Успешно", "NDVI загружен из CSV (" + idx + " зон)", Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            showAlert("Ошибка", "Не удалось прочитать файл: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ==================== ПРОГНОЗ ====================

    @FXML
    private void calculateForecast() {
        if (currentField == null) {
            showAlert("Ошибка", "Выберите поле!", Alert.AlertType.WARNING);
            return;
        }
        try {
            String[] tempsStr = forecastTempsField.getText().isEmpty()
                    ? new String[0] : forecastTempsField.getText().split(",");
            String[] rainsStr = forecastRainsField.getText().isEmpty()
                    ? new String[0] : forecastRainsField.getText().split(",");

            int days = Math.max(3, Math.max(tempsStr.length, rainsStr.length));
            double[] temps = new double[days];
            double[] rains = new double[days];
            for (int i = 0; i < days; i++) {
                temps[i] = i < tempsStr.length
                        ? Double.parseDouble(tempsStr[i].trim()) : airTempSpinner.getValue();
                rains[i] = i < rainsStr.length
                        ? Double.parseDouble(rainsStr[i].trim()) : precipitationSpinner.getValue();
            }
            forecastArea.setText(calculator.forecastStress(currentField.getAverageStress(), temps, rains));
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Введите числа через запятую (например: 30,32,28)", Alert.AlertType.ERROR);
        }
    }

    // ==================== ИСТОРИЯ ====================

    @FXML
    private void saveCurrentSnapshot() {
        if (currentField == null) return;

        List<Zone> snap = new ArrayList<>();
        for (Zone z : currentField.getZones()) {
            Zone copy = new Zone(z.getRow(), z.getCol());
            copy.setNdvi(z.getNdvi());
            copy.setSoilTemp(z.getSoilTemp());
            copy.setSoilMoisture(z.getSoilMoisture());
            copy.setSolarRadiation(z.getSolarRadiation());
            copy.setStressIndex(z.getStressIndex());
            copy.setStatus(z.getStatus());
            snap.add(copy);
        }

        LocalDate date = surveyDatePicker.getValue() != null ? surveyDatePicker.getValue() : LocalDate.now();
        String weather = String.format("T=%.0f°C, ос=%.0fмм, влажн=%.0f%%, рад=%.1f",
                airTempSpinner.getValue(), precipitationSpinner.getValue(),
                airHumiditySpinner.getValue(), radiationSpinner.getValue());

        HistoryEntry entry = new HistoryEntry(
                date,
                currentField.getAverageNDVI(),
                currentField.getAverageStress(),
                weather,
                calculator.generateRecommendations(currentField),
                snap);

        currentField.getHistory().add(entry);
        fileService.saveHistory(currentField);
        updateHistoryTable();
        updateLineChart();
        showAlert("Сохранено", "Съёмка добавлена в историю", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void compareWithPrevious() {
        if (currentField == null) return;
        List<HistoryEntry> history = currentField.getHistory();
        if (history.size() < 2) {
            showAlert("Информация", "Нужно минимум 2 съёмки для сравнения.", Alert.AlertType.INFORMATION);
            return;
        }
        HistoryEntry prev = history.get(history.size() - 2);
        HistoryEntry curr = history.get(history.size() - 1);
        if (prev.getZoneSnapshots() == null || curr.getZoneSnapshots() == null
                || prev.getZoneSnapshots().isEmpty()) {
            showAlert("Информация", "Данные зон для сравнения отсутствуют (сохраните съёмки заново).", Alert.AlertType.INFORMATION);
            return;
        }

        compareMapGrid.getChildren().clear();
        int size = currentField.getGridSize();
        double cellSize = Math.max(45, 360.0 / size);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int idx = i * size + j;
                if (idx >= prev.getZoneSnapshots().size() || idx >= curr.getZoneSnapshots().size()) continue;

                double prevNDVI = prev.getZoneSnapshots().get(idx).getNdvi();
                double currNDVI = curr.getZoneSnapshots().get(idx).getNdvi();
                double delta = currNDVI - prevNDVI;

                Color color;
                if (delta > 0.05)       color = Color.GREEN;
                else if (delta < -0.05) color = Color.RED;
                else                    color = Color.YELLOW;

                Rectangle rect = new Rectangle(cellSize, cellSize);
                rect.setFill(color);
                rect.setStroke(Color.BLACK);
                rect.setStrokeWidth(0.5);

                Label lbl = new Label(String.format("%+.2f", delta));
                lbl.setStyle("-fx-font-size: 10; -fx-font-weight: bold;");

                VBox vbox = new VBox(rect, lbl);
                vbox.setAlignment(Pos.CENTER);
                compareMapGrid.add(vbox, j, i);
            }
        }
    }

    // ==================== ОТОБРАЖЕНИЕ ====================

    private void showCurrentField() {
        if (currentField == null) return;

        // Загрузить историю из файла если ещё не загружена
        if (currentField.getHistory().isEmpty()) {
            currentField.getHistory().addAll(fileService.loadHistory(currentField));
        }

        zonesTableView.setItems(FXCollections.observableArrayList(currentField.getZones()));
        updateHeatMap();
        updateStatistics();
        updateRecommendations();
        updateHistoryTable();
        updateLineChart();
        updateBarChart();
    }

    private void updateHeatMap() {
        heatMapGrid.getChildren().clear();
        int size = currentField.getGridSize();
        double cellSize = Math.max(50, 380.0 / size);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Zone zone = currentField.getZones().get(i * size + j);
                Rectangle rect = new Rectangle(cellSize, cellSize);
                rect.setFill(ColorUtils.getStressColor(zone.getStressIndex()));
                rect.setStroke(Color.BLACK);
                rect.setStrokeWidth(0.5);

                Label label = new Label(String.format("%.2f", zone.getNdvi()));
                label.setTextFill(Color.WHITE);
                label.setStyle("-fx-font-weight: bold; -fx-font-size: 11;");

                VBox vbox = new VBox(rect, label);
                vbox.setAlignment(Pos.CENTER);
                vbox.setOnMouseClicked(e -> editZone(zone));
                heatMapGrid.add(vbox, j, i);
            }
        }
    }

    private void editZone(Zone zone) {
        TextInputDialog dialog = new TextInputDialog(String.format("%.3f", zone.getNdvi()));
        dialog.setTitle("Редактирование зоны");
        dialog.setHeaderText("Зона " + zone.getZoneName());
        dialog.setContentText("NDVI (-1.0 до 1.0):");
        dialog.showAndWait().ifPresent(str -> {
            try {
                zone.setNdvi(Double.parseDouble(str));
                calculateStress();
            } catch (Exception e) {
                showAlert("Ошибка", "Некорректное значение NDVI!", Alert.AlertType.ERROR);
            }
        });
    }

    private void updateStatistics() {
        if (currentField == null) return;
        double avgNDVI = currentField.getAverageNDVI();
        double stress  = currentField.getAverageStress();
        long total     = currentField.getZones().size();

        avgNDVILabel.setText(String.format("%.3f", avgNDVI));
        stressIndexLabel.setText(String.format("%.1f%%", stress));

        if (total > 0) {
            long healthy  = currentField.getZones().stream().filter(z -> z.getStressIndex() < 30).count();
            long critical = currentField.getZones().stream().filter(z -> z.getStressIndex() >= 60).count();
            healthyZonesLabel.setText(String.format("%.0f%%", 100.0 * healthy / total));
            criticalZonesLabel.setText(String.format("%.0f%%", 100.0 * critical / total));
        }
    }

    private void updateRecommendations() {
        if (currentField != null)
            recommendationsArea.setText(calculator.generateRecommendations(currentField));
    }

    private void updateHistoryTable() {
        if (currentField != null)
            historyTableView.setItems(FXCollections.observableArrayList(currentField.getHistory()));
    }

    private void updateLineChart() {
        ndviLineChart.getData().clear();
        if (currentField == null || currentField.getHistory().isEmpty()) return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Средний NDVI");
        for (HistoryEntry e : currentField.getHistory()) {
            series.getData().add(new XYChart.Data<>(e.getDate().toString(), e.getAvgNDVI()));
        }
        ndviLineChart.getData().add(series);
    }

    private void updateBarChart() {
        stressBarChart.getData().clear();
        if (currentField == null || currentField.getZones().isEmpty()) return;
        long low  = currentField.getZones().stream().filter(z -> z.getStressIndex() < 30).count();
        long med  = currentField.getZones().stream().filter(z -> z.getStressIndex() >= 30 && z.getStressIndex() < 60).count();
        long high = currentField.getZones().stream().filter(z -> z.getStressIndex() >= 60).count();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Низкий (<30%)", low));
        series.getData().add(new XYChart.Data<>("Средний (30-60%)", med));
        series.getData().add(new XYChart.Data<>("Высокий (>60%)", high));
        stressBarChart.getData().add(series);
    }

    // ==================== ОТЧЁТЫ ====================

    @FXML
    private void exportReport() {
        if (currentField == null) {
            showAlert("Ошибка", "Выберите поле!", Alert.AlertType.WARNING);
            return;
        }
        fileService.exportReport(currentField);
        showAlert("Экспорт", "Отчёт сохранён в папку data/", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void exportMapAsPNG() {
        if (heatMapGrid.getChildren().isEmpty()) {
            showAlert("Ошибка", "Сначала отобразите тепловую карту!", Alert.AlertType.WARNING);
            return;
        }
        fileService.exportHeatMapAsPNG(heatMapGrid, currentField != null ? currentField.getName() : "map");
        showAlert("Инфо", "Экспорт PNG пока в упрощённом режиме.", Alert.AlertType.INFORMATION);
    }

    // ==================== УТИЛИТЫ ====================

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
