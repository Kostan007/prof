module com.example.agrimonitor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;     // ← теперь будет найдено

    exports com.example.agrimonitor;
    exports com.example.agrimonitor.controller;
    exports com.example.agrimonitor.model;
    exports com.example.agrimonitor.service;
    exports com.example.agrimonitor.exception;
    exports com.example.agrimonitor.util;

    opens com.example.agrimonitor.controller;
    opens com.example.agrimonitor.model;
}