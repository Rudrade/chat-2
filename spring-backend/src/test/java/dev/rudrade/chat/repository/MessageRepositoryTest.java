package dev.rudrade.chat.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import dev.rudrade.chat.SqlIntegrationTest;

@DataJpaTest
@Sql(
    scripts = {"/sql-scripts/users.sql","/sql-scripts/messages.sql"},
    executionPhase = ExecutionPhase.BEFORE_TEST_CLASS
)
class MessageRepositoryTest extends SqlIntegrationTest {

    @Autowired private MessageRepository messageRepository;
    @Autowired private UserRepository userRepository;

    //===============
    //  findLatest
    //===============

    @Test
    void itShouldFindLatest() {
        var result = messageRepository.findLatest(UUID.fromString("29a8d960-46d2-4e55-80ab-7f6477541a28"), "temp", PageRequest.of(0, 10));

        assertThat(result).hasSize(3);
        var users = new ArrayList<UUID>(); 
        var messages = new ArrayList<String>();

        result.forEach(ms -> {
            if (ms.userId()!=null) users.add(ms.userId());
            if (ms.text()!=null) messages.add(ms.text());
        });

        assertThat(messages).hasSize(2).containsExactlyInAnyOrder("msg-2", "msg-3");

        var usersDb = new ArrayList<UUID>();
        userRepository.findAll().forEach(u -> {
            if (u.isActive() && u.getName().toLowerCase().contains("temp")) {
                usersDb.add(u.getId());
            }
        });

        assertThat(users).hasSize(3).containsExactlyInAnyOrderElementsOf(usersDb);
    }

}
