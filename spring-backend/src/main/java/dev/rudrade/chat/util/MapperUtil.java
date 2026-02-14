package dev.rudrade.chat.util;

import java.util.Objects;

import dev.rudrade.chat.dto.MessageDto;
import dev.rudrade.chat.dto.MessageSummaryDto;
import dev.rudrade.chat.dto.UserDto;
import dev.rudrade.chat.model.Message;
import dev.rudrade.chat.model.MessageSummary;
import dev.rudrade.chat.model.User;

public class MapperUtil {

    private MapperUtil() {}

    public static UserDto userDto(User user) {
        Objects.requireNonNull(user, "user must not be null");

        return new UserDto(user.getId(), user.getUsername(), user.getName());
    }

    public static MessageSummaryDto messageSummaryDto(MessageSummary messageSummary) {
        Objects.requireNonNull(messageSummary, "messageSummary must not be null");

        return new MessageSummaryDto(
            messageSummary.chatId(),
            messageSummary.userId(),
            messageSummary.name(),
            false,
            messageSummary.text(),
            messageSummary.dtCreated(),
            null
        );
    }

    public static MessageDto messageDto(Message message) {
        Objects.requireNonNull(message, "message must not be null");

        return new MessageDto(
            message.getId(),
            message.getUser().getId(),
            message.getChat().getId(),
            message.getText(),
            message.getDtCreated()
        );
    }

}
