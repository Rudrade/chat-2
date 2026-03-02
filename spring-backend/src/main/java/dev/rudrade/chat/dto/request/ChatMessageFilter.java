package dev.rudrade.chat.dto.request;

import java.util.UUID;

public record ChatMessageFilter(UUID chatId, Integer offset, Integer limit) {

}
