package dev.rudrade.chat.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.rudrade.chat.exception.InvalidDataException;
import jakarta.validation.Validation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

class ValidationUtilTest {

    private ValidationUtil target;

    @BeforeEach
    void init() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            target = new ValidationUtil(factory.getValidator());
        }
    }

    //=============
    //  validate
    //=============

    @Test    
    void itShouldThrowWhenIsNull() {
        assertThrows(InvalidDataException.class, 
            () -> target.validate(null)
        );
    }

    @Test
    void itShouldThrowWhenIsInvalid() {
        var obj = new TragetObj(null, "     ");

        var result = assertThrows(InvalidDataException.class, 
            () -> target.validate(obj)
        );

        assertThat(result.getErrors())
            .isNotNull()
            .hasSize(2);
    }

    @Test
    void itShouldValidate() {
        var obj = new TragetObj("valid", "valid");

        assertDoesNotThrow(() -> target.validate(obj));
    }

    private final record TragetObj(@NotNull String arg0, @NotBlank String agr1) {}
}
