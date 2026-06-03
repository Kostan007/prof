package com.example.agrimonitor.exception;

public class EmptyFieldException extends Exception {

    public EmptyFieldException() {
        super("Поле не содержит зон для мониторинга");
    }

    public EmptyFieldException(String message) {
        super(message);
    }
}