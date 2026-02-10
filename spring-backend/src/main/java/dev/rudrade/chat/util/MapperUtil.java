package dev.rudrade.chat.util;

import dev.rudrade.chat.dto.MessageSummaryDto;
import dev.rudrade.chat.dto.UserDto;
import dev.rudrade.chat.model.MessageSummary;
import dev.rudrade.chat.model.User;

public class MapperUtil {

    private MapperUtil() {}

    public static UserDto userDto(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getName());
    }

    public static MessageSummaryDto messageSummaryDto(MessageSummary messageSummary) {
        return new MessageSummaryDto(
            messageSummary.chatId(),
            messageSummary.name(),
            false,
            messageSummary.text(),
            messageSummary.dtCreated(),
            null
            );

    }

}
