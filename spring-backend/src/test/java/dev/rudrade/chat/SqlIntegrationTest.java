package dev.rudrade.chat;

import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public abstract class SqlIntegrationTest {

    @Container
    static SqlContainer sqlContainer = new SqlContainer();
    static {
        if (!sqlContainer.isRunning()) {
            sqlContainer.start();
            sqlContainer.withConnectTimeoutSeconds(5);
        }
        
    }

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", sqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", sqlContainer::getUsername);
        registry.add("spring.datasource.password", sqlContainer::getPassword);
    }
}

class SqlContainer extends PostgreSQLContainer {

    SqlContainer() {
        super("postgres:18");
    }

    @Override
    public void close() {
        // Do nothing, JVM closes upon test completion
    }

}