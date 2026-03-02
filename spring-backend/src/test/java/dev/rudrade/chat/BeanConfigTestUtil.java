package dev.rudrade.chat;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;

public class BeanConfigTestUtil {

    @TestConfiguration
    public static class SecurityEvaluationContextExtensionBean {
        @Bean
        SecurityEvaluationContextExtension securityEvaluationContextExtension() {
            return new SecurityEvaluationContextExtension();
        }
    }

}
