package dev.rudrade.chat.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import dev.rudrade.chat.dto.MessageDto;
import dev.rudrade.chat.dto.MessageInputDto;

@Controller
public class MessageController { // TODO: Secure this

    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public MessageDto send(MessageInputDto message) {
        return new MessageDto(
            UUID.randomUUID(),
            UUID.randomUUID(),
            message.idTo(),
            message.text(),
            LocalDate.now());
    }

}
