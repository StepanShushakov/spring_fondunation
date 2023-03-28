package org.example.app.exceptions;

public class InvalidUploadDataException extends Exception{

    private final String message;

    public InvalidUploadDataException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
