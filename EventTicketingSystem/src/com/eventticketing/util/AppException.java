package com.eventticketing.util;

/** Unchecked exception carrying a user-friendly message (validation, database, I/O). */
public class AppException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
