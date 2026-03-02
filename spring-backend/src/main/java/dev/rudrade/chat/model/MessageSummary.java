package dev.rudrade.chat.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageSummary(String name, UUID chatId, String text, LocalDateTime dtCreated, UUID userId) {

}
