package dev.rudrade.chat.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import dev.rudrade.chat.SqlIntegrationTest;
import dev.rudrade.chat.model.MessageSummary;

@DataJpaTest
@Sql(
    scripts = {"/sql-scripts/users.sql"},
    executionPhase = ExecutionPhase.BEFORE_TEST_CLASS
)
class MessageRepositoryTest extends SqlIntegrationTest {

    @Autowired private MessageRepository messageRepository;
    @Autowired private UserRepository userRepository;

    //===============
    //  findByChat
    //===============


    @Sql("/sql-scripts/messages.sql")
    @Test
    void itShouldFindByChat() {
        var chatId = UUID.fromString("01415185-b265-4362-b56f-fc7829e61cf0");
        
        var result = messageRepository.findByChat(chatId, PageRequest.of(0, 10));
        assertNotNull(result);
        assertEquals(10, result.getSize());
        assertEquals(100, result.getTotalElements());

        var idFrom = UUID.fromString("4a33e6ec-215a-411f-ac8c-09d7beaa77dc");
        assertThat(result).allSatisfy(msg -> {
            assertNotNull(msg.id());
            assertEquals(idFrom, msg.idFrom());
            assertEquals(chatId, msg.idChatTo());
            assertEquals("msg", msg.text());
            assertThat(msg.dtSent()).isNotNull().isCloseToUtcNow(within(1, ChronoUnit.MINUTES));
        });
    }

    //===========================
    //  findLatestWithMessages
    //===========================

    @Sql("/sql-scripts/messages-latest.sql")
    @Test
    void itShouldFindLatestWithMessages() {
        var userId = UUID.fromString("a62f1573-24b2-49af-81d4-1fb2ee1ff208"); // TEMP-D

        var result = messageRepository.findLatestWithMessages(userId, PageRequest.of(0, 10));

        assertThat(result).hasSize(2);

        var chatIds = result.stream().map(MessageSummary::chatId).toList();
        assertThat(chatIds).containsExactlyInAnyOrder(
            UUID.fromString("8b669447-ac5c-4a7d-ba40-e286d434fb78"),
            UUID.fromString("48bceef2-f02e-438e-b747-5435851bd14d")
        );
    }

    @Sql("/sql-scripts/messages-latest.sql")
    @Test
    void itShouldFindLatestWithMessagesWhenUserHasNoChats() {
        var userId = UUID.fromString("ff02df0d-1d76-4262-b00b-8de3bac55a49"); // TEMP-B

        var result = messageRepository.findLatestWithMessages(userId, PageRequest.of(0, 10));

        assertThat(result).isEmpty();
    }

    //===============
    //  findLatest
    //===============

    @Sql({"/sql-scripts/messages-summary.sql", "/sql-scripts/messages-latest.sql"})
    @Test
    void itShouldFindLatest() {
        var userId = UUID.fromString("29a8d960-46d2-4e55-80ab-7f6477541a28");
        var page = PageRequest.of(0, 20);
        var result = messageRepository.findLatest(userId, "temp", page);

        assertThat(result).hasSize(10);
        assertThat(result)
            .usingRecursiveFieldByFieldElementComparator()
            .doesNotHaveDuplicates();

        var users = new ArrayList<UUID>(); 
        var messages = new ArrayList<String>();

        result.forEach(ms -> {
            if (ms.userId()!=null) users.add(ms.userId());
            if (ms.text()!=null) messages.add(ms.text());
        });

        assertThat(messages).hasSize(4).containsExactlyInAnyOrder("msg-2", "msg-3", "msg-5-1", "msg-5-2");

        var usersDb = new ArrayList<UUID>();
        usersDb.add(UUID.fromString("aee5f52f-f10e-4b93-abbd-28303c3b9b2e"));
        userRepository.findAll().forEach(u -> {
            if (u.isActive() && u.getName().toLowerCase().contains("temp")) {
                usersDb.add(u.getId());
            }
        });

        assertThat(users).hasSize(10).containsAnyElementsOf(usersDb);
    }

}
