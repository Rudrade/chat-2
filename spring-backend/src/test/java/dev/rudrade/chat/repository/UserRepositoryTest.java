package dev.rudrade.chat.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.Principal;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import dev.rudrade.chat.SecurityTestUtil;
import dev.rudrade.chat.SqlIntegrationTest;
import dev.rudrade.chat.BeanConfigTestUtil.SecurityEvaluationContextExtensionBean;

@DataJpaTest
@Sql(
    scripts = "/sql-scripts/users.sql",
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD
)
@Import(SecurityEvaluationContextExtensionBean.class)
class UserRepositoryTest extends SqlIntegrationTest {

    @Autowired private UserRepository userRepository;

    @Mock private Principal principal;

    //===================
    //  findByUsername
    //===================
    @Test
    void itShouldFindByUsername() {
        var username = "user-test";

        var result = userRepository.findByUsername(username);
        assertThat(result)
            .isNotNull()
            .isPresent()
            .get()
            .satisfies(u -> {
                assertEquals(username, u.getUsername());
            });
    }

    //================
    //  findDetails
    //================

    @Test
    void itShouldFindDetails() {
        var userId = "29a8d960-46d2-4e55-80ab-7f6477541a28";
        var username = "user-test";

        SecurityTestUtil.builder()
            .withId(userId)
            .build().authenticate();

        var result = userRepository.findDetails();
        assertThat(result)
            .isNotNull()
            .isPresent()
            .get()
            .satisfies(u -> {
                assertEquals(userId, u.getId().toString());
                assertEquals(username, u.getUsername());
            });
    }
}
