package dev.rudrade.chat.util;

import dev.rudrade.chat.exception.InvalidDataException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ValidationUtil {

    private final Validator validator;

    public void validate(Object arg0) {
        var result = validator.validate(arg0);
        if (!result.isEmpty()) throw new InvalidDataException(result);
    }

}
