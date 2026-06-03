package com.example.agrimonitor.exception;

public class InvalidNDVIException extends Exception {

    public InvalidNDVIException() {
        super("NDVI должен быть в диапазоне от -1.0 до 1.0");
    }

    public InvalidNDVIException(String message) {
        super(message);
    }
}