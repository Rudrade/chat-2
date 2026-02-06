package dev.rudrade.chat.dto;

import java.util.UUID;

public record MessageInputDto(UUID idTo, String text) {

}
