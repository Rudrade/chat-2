package dev.rudrade.chat.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public record MessageInputDto(UUID idChat, UUID idUserTo, @NotBlank String text) {

}
