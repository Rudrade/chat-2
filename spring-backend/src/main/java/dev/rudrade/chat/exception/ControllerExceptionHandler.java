package dev.rudrade.chat.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.validation.ConstraintViolationException;

import java.util.Set;

@ControllerAdvice
public class ControllerExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<Error> handleConstraintViolation(ConstraintViolationException ex) {
        return handleInvalidData(new InvalidDataException(ex.getConstraintViolations()));
    }

    @ExceptionHandler(Throwable.class)
    ResponseEntity<Error> handleThrowable(Throwable ex) {
        LOGGER.error("", ex);
        return new ResponseEntity<>(new Error("Unxpected error", null), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidAccessException.class)
    ResponseEntity<Error> handleInvalidAccess(InvalidAccessException ex) {
        return convert(ex, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<Error> handleUserNotFound(UserNotFoundException ex) {
        return convert(ex, HttpStatus.UNAUTHORIZED);
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
