package com.example.agrimonitor.controller;

import com.example.agrimonitor.model.Field;
import com.example.agrimonitor.model.Zone;
import com.example.agrimonitor.model.HistoryEntry;
import com.example.agrimonitor.service.StressCalculator;
import com.example.agrimonitor.service.FileService;
import com.example.agrimonitor.util.ColorUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    // === Поля ===
    @FXML private TextField fieldNameField;
    @FXML private ComboBox<String> cropComboBox;
    @FXML private ComboBox<String> phaseComboBox;
    @FXML private Spinner<Double> areaSpinner;
    @FXML private Spinner<Double> centerXSpinner;
    @FXML private Spinner<Double> centerYSpinner;
    @FXML private Spinner<Integer> gridSizeSpinner;
    @FXML private ListView<Field> fieldsListView;

    // === Мониторинг ===
    @FXML private Spinner<Double> airTempSpinner;
    @FXML private Spinner<Double> precipitationSpinner;
    @FXML private Spinner<Double> airHumiditySpinner;

    @FXML private TableView<Zone> zonesTableView;
    @FXML private TableColumn<Zone, String> zoneColumn;
    @FXML private TableColumn<Zone, Double> ndviColumn;
    @FXML private TableColumn<Zone, Double> soilTempColumn;
    @FXML private TableColumn<Zone, Double> soilMoistureColumn;
    @FXML private TableColumn<Zone, Double> stressColumn;
    @FXML private TableColumn<Zone, String> statusColumn;

    @FXML private GridPane heatMapGrid;
    @FXML private Label avgNDVILabel;
    @FXML private Label stressIndexLabel;
    @FXML private TextArea recommendationsArea;

    // === История ===
    @FXML private TableView<HistoryEntry> historyTableView;
    @FXML private LineChart<String, Number> ndviLineChart;

    private ObservableList<Field> fields = FXCollections.observableArrayList();
    private Field currentField = null;

    private final StressCalculator calculator = new StressCalculator();
    private final FileService fileService = new FileService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupSpinners();
        setupWeatherSpinners();
        setupComboBoxes();
        setupTable();

        fieldsListView.setItems(fields);
        fieldsListView.getSelectionModel().selectedItemProperty().addListener((obs, old, newField) -> {
            currentField = newField;
            if (newField != null) showCurrentField();
        });

        fields.addAll(fileService.loadAllFields());
    }

    private void setupSpinners() {
        areaSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1, 1000, 50, 0.5));
        centerXSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100, 50, 1));
        centerYSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 100, 50, 1));
        gridSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(3, 8, 4));
    }

    private void setupWeatherSpinners() {
        airTempSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-10, 45, 22, 0.5));
        precipitationSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 200, 15, 1));
        airHumiditySpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(20, 100, 65, 1));
        // radiationSpinner убрали, чтобы не было ошибки
    }

    private void setupComboBoxes() {
        cropComboBox.getItems().addAll("Пшеница", "Кукуруза", "Подсолнечник", "Соя", "Томаты", "Огурцы");
        phaseComboBox.getItems().addAll("Всходы", "Вегетация", "Цветение", "Созревание");
        cropComboBox.setValue("Пшеница");
        phaseComboBox.setValue("Вегетация");
    }

    private void setupTable() {
        zoneColumn.setCellValueFactory(new PropertyValueFactory<>("zoneName"));
        ndviColumn.setCellValueFactory(new PropertyValueFactory<>("ndvi"));
        soilTempColumn.setCellValueFactory(new PropertyValueFactory<>("soilTemp"));
        soilMoistureColumn.setCellValueFactory(new PropertyValueFactory<>("soilMoisture"));
        stressColumn.setCellValueFactory(new PropertyValueFactory<>("stressIndex"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

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
    private void calculateStress() {
        if (currentField == null) return;
        currentField.calculateAllStress(calculator);
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
        double precip = precipitationSpinner.getValue();

        for (Zone zone : currentField.getZones()) {
            zone.setSoilTemp(airTemp + (Math.random() * 4 - 2));
            zone.setSoilMoisture(Math.max(10, Math.min(90, 50 + (precip * 0.8) - (airTemp * 0.5))));
        }

        showAlert("Успешно", "Метеоданные применены!", Alert.AlertType.INFORMATION);
        calculateStress();
    }

    @FXML
    private void generateRandomNDVI() {
        if (currentField == null) return;
        currentField.getZones().forEach(Zone::generateRandomNDVI);
        calculateStress();
    }

    private void showCurrentField() {
        if (currentField == null) return;
        zonesTableView.setItems(FXCollections.observableArrayList(currentField.getZones()));
        updateHeatMap();
        updateStatistics();
        updateRecommendations();
    }

    private void updateHeatMap() {
        heatMapGrid.getChildren().clear();
        int size = currentField.getGridSize();
        double cellSize = Math.max(55, 420 / size);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Zone zone = currentField.getZones().get(i * size + j);
                Rectangle rect = new Rectangle(cellSize, cellSize);
                rect.setFill(ColorUtils.getStressColor(zone.getStressIndex()));
                rect.setStroke(Color.BLACK);

                Label label = new Label(String.format("%.2f", zone.getNdvi()));
                label.setTextFill(Color.WHITE);
                label.setStyle("-fx-font-weight: bold;");

                VBox vbox = new VBox(rect, label);
                vbox.setAlignment(Pos.CENTER);
                vbox.setOnMouseClicked(e -> editZone(zone));

                heatMapGrid.add(vbox, j, i);
            }
        }
    }

    private void editZone(Zone zone) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(zone.getNdvi()));
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
        double avg = currentField.getAverageNDVI();
        double stress = currentField.getAverageStress();
        avgNDVILabel.setText(String.format("%.3f", avg));
        stressIndexLabel.setText(String.format("%.1f%%", stress));
    }

    private void updateRecommendations() {
        recommendationsArea.setText(calculator.generateRecommendations(currentField));
    }

    @FXML
    private void saveCurrentSnapshot() {
        if (currentField == null) return;
        HistoryEntry entry = new HistoryEntry(LocalDate.now(),
                currentField.getAverageNDVI(),
                currentField.getAverageStress(),
                calculator.generateRecommendations(currentField));
        currentField.getHistory().add(entry);
        updateHistoryTable();
        showAlert("Сохранено", "Съёмка добавлена в историю", Alert.AlertType.INFORMATION);
    }

    private void updateHistoryTable() {
        if (currentField != null) {
            historyTableView.setItems(FXCollections.observableArrayList(currentField.getHistory()));
        }
    }

    @FXML
    private void exportReport() {
        if (currentField == null) {
            showAlert("Ошибка", "Выберите поле!", Alert.AlertType.WARNING);
            return;
        }
        fileService.exportReport(currentField);
        showAlert("Экспорт", "Отчёт успешно сохранён в папку data/", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void exportMapAsPNG() {
        if (heatMapGrid.getChildren().isEmpty()) {
            showAlert("Ошибка", "Сначала отобразите тепловую карту!", Alert.AlertType.WARNING);
            return;
        }

        showAlert("Инфо", "Экспорт карты в PNG пока в упрощённом режиме.\nКарта сохранена в data/map.png", Alert.AlertType.INFORMATION);
        fileService.exportHeatMapAsPNG(heatMapGrid, currentField != null ? currentField.getName() : "map");
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}