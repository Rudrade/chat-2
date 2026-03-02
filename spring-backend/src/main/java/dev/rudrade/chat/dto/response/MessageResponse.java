package dev.rudrade.chat.dto.response;

import java.util.List;

import dev.rudrade.chat.dto.MessageDto;

public record MessageResponse(List<MessageDto> messages, long count) {

}
