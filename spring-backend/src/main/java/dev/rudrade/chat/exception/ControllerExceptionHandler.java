package dev.rudrade.chat.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Set;

@ControllerAdvice
public class ControllerExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @ExceptionHandler(Throwable.class)
    ResponseEntity<Error> handleThrowable(Throwable ex) {
        LOGGER.error("", ex);
        return convert(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidAccessException.class)
    ResponseEntity<Error> handleInvalidAccess(InvalidAccessException ex) {
        return convert(ex, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InvalidDataException.class)
    ResponseEntity<Error> handleInvalidData(InvalidDataException ex) {
        return new ResponseEntity<>(new Error(ex.getMessage(), ex.getErrors()), HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Error> convert(Throwable ex, HttpStatus status) {
        return new ResponseEntity<>(new Error(ex.getMessage(), null), status);
    }

    private record Error(String message, Set<String> errors) {}
}
