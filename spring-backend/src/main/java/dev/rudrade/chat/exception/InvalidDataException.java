package dev.rudrade.chat.exception;

import jakarta.validation.ConstraintViolation;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class InvalidDataException extends RuntimeException {
    private final Set<? extends ConstraintViolation<?>> constraints;
    private final String message;

    public InvalidDataException(Set<? extends ConstraintViolation<?>> constraints) {
        this.constraints = constraints;
        this.message = null;
    }

    public InvalidDataException(String message) {
        this.constraints = null;
        this.message = message;
    }

    public Set<String> getErrors() {
        var capacity = constraints == null ? 0 :constraints.size();
        Set<String> errors = HashSet.newHashSet(capacity);
        if (capacity == 0) return errors;

        constraints.forEach(constraint -> errors.add(constraint.getMessage()));
        return errors;
    }
}
