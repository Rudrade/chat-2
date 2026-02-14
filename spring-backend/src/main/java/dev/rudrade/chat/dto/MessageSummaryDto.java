package dev.rudrade.chat.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageSummaryDto(UUID chatId, UUID userId, String name, boolean online, String lastMessage, LocalDateTime dtSent, String image) {

}
