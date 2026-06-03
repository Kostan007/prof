package com.example.agrimonitor.exception;

public class NoHistoricalDataException extends Exception {

    public NoHistoricalDataException() {
        super("Отсутствует история данных для сравнения");
    }

    public NoHistoricalDataException(String message) {
        super(message);
    }
}