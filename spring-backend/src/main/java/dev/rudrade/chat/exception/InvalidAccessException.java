package dev.rudrade.chat.exception;

public class InvalidAccessException extends RuntimeException {

    public static final String INVALID_ACCESS = "Invalid access";
    public static final String USER_NOT_FOUND = "User not found";

    public InvalidAccessException() {
        this(INVALID_ACCESS);
    }

    public InvalidAccessException(String message) {
        super(message);
    }
}
