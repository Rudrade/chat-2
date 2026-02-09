package dev.rudrade.chat.config;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import dev.rudrade.chat.util.ValidationUtil;

@Configuration
public class AppConfig {

    private Validator validation() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator();
        }
    }

    @Bean
    ValidationUtil validationUtil() {
        return new ValidationUtil(validation());
    }

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(16);
    }
}
