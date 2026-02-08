package dev.rudrade.chat.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidAccessException extends AuthenticationException {

    public InvalidAccessException() {
        super("Invalid access");
    }
}
