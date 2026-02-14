package dev.rudrade.chat.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import dev.rudrade.chat.SqlIntegrationTest;
import dev.rudrade.chat.model.Chat.ChatType;

@DataJpaTest
@Sql(
    scripts = {"/sql-scripts/users.sql","/sql-scripts/chats.sql"},
    executionPhase = ExecutionPhase.BEFORE_TEST_CLASS
)
class ChatRepositoryTest extends SqlIntegrationTest {

    @Autowired private ChatRepository repository;

    @Test
    void itShouldFindByIdAndUser() {
        var userId = UUID.fromString("29a8d960-46d2-4e55-80ab-7f6477541a28");
        var chatId = UUID.fromString("c7ff9617-98a8-4f12-8d01-d5b0870b0967");

        var result = repository.findByIdAndUser(chatId, userId);
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(chatId, result.get().getId());
    }

    //=====================
    //  findByTypeAndUsers
    //=====================

    @Test
    void itShouldFindByExactUsers3Members() {
        var usersId = List.of(
            UUID.fromString("29a8d960-46d2-4e55-80ab-7f6477541a28"),
            UUID.fromString("29a8d960-46d2-4e55-80ab-7f6477541d28"),
            UUID.fromString("29a8d960-46d2-4e55-80ac-7f6477541d28")
        );

        var result = repository.findByTypeAndUsers(ChatType.GROUP.name(), usersId);
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(UUID.fromString("c7ff9617-98a8-4f12-8d01-d5b0870b0960"), result.get().getId());
    }

    @Test
    void itShouldFindByExactUsers2Members() {
        var usersId = List.of(
            UUID.fromString("29a8d960-46d2-4e55-80ab-7f6477541a28"),
            UUID.fromString("29a8d960-46d2-4e55-80ab-7f6477541d28")
        );

        var result = repository.findByTypeAndUsers(ChatType.ONE.name(), usersId);
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(UUID.fromString("c7ff9617-98a8-4f12-8d01-d5b0870b0967"), result.get().getId());
    }

}
