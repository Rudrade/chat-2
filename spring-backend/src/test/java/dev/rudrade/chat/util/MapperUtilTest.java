package dev.rudrade.chat.util;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import dev.rudrade.chat.model.MessageSummary;
import dev.rudrade.chat.model.User;
import dev.rudrade.chat.model.Chat;
import dev.rudrade.chat.model.Message;

class MapperUtilTest {

    @Test
    void itShouldMapUserDto() {
        var user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("test");
        user.setName("name test");

        var result = MapperUtil.userDto(user);

        assertThat(result)
            .isNotNull()
            .satisfies(arg -> {
                assertEquals(arg.id(), user.getId());
                assertEquals(arg.username(), user.getUsername());
                assertEquals(arg.name(), user.getName());
            });
    }

    @Test
    void itShouldMapMessageSummary() {
        var summary = new MessageSummary(
            "name 123", 
            UUID.randomUUID(), 
            "test 456", 
            LocalDateTime.now(), 
            UUID.randomUUID()
        );

        var result = MapperUtil.messageSummaryDto(summary);

        assertThat(result)
            .isNotNull()
            .satisfies(arg -> {
                assertEquals(arg.chatId(), summary.chatId());
                assertEquals(arg.name(), summary.name());
                assertFalse(arg.online());
                assertEquals(arg.lastMessage(), summary.text());
                assertEquals(arg.dtSent(), summary.dtCreated());
                assertNull(arg.image());
            });
    }

    @Test
    void itShouldMapMessageDto() {
        var chat = new Chat();
        chat.setId(UUID.randomUUID());

        var user = new User();
        user.setId(UUID.randomUUID());

        var message = new Message();
        message.setId(UUID.randomUUID());
        message.setText("text sent");
        message.setDtCreated(LocalDateTime.now());
        message.setChat(chat);
        message.setUser(user);

        var result = MapperUtil.messageDto(message);

        assertThat(result)
            .isNotNull()
            .satisfies(arg -> {
                assertEquals(message.getId(), arg.id());
                assertEquals(user.getId(), arg.idFrom());
                assertEquals(chat.getId(), arg.idChatTo());
                assertEquals(message.getText(), arg.text());
                assertEquals(message.getDtCreated(), arg.dtSent());
            });
    }

}
