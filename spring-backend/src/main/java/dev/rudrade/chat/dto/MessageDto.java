package dev.rudrade.chat.dto;

import java.time.LocalDate;
import java.util.UUID;

public record MessageDto(UUID id, UUID idFrom, UUID idTo, String text, LocalDate dtSent) {

}
