package dev.rudrade.chat.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageDto(UUID id, UUID idFrom, UUID idChatTo, String text, LocalDateTime dtSent) {

}
