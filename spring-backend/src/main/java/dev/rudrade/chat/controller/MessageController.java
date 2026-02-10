package dev.rudrade.chat.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;

import dev.rudrade.chat.dto.MessageDto;
import dev.rudrade.chat.dto.MessageInputDto;
import dev.rudrade.chat.dto.MessageSummaryDto;
import dev.rudrade.chat.service.MessageService;
import dev.rudrade.chat.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final SimpUserRegistry registry;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    @MessageMapping("/sendMessage")
    public void send(MessageInputDto message) {
        log.debug("Start sendMessage");
        var payload = new MessageDto(
            UUID.randomUUID(),
            UUID.randomUUID(),
            message.idTo(),
            message.text(),
            LocalDate.now());
        
        var subscribers = registry.getUsers().stream().map(SimpUser::getName).toList();
        log.trace("subscribers:"+subscribers);
        subscribers.forEach(sub -> {
            log.trace("sending to:"+sub);
            messagingTemplate.convertAndSendToUser(sub, "/topic/messages", payload); // TODO: Send to chat users connected
        });
        log.debug("End sendMessage");
    }

    @MessageMapping("/search/{term}")
    @SendTo("/topic/summaries")
    public List<MessageSummaryDto> findSummaries(@DestinationVariable String term, Principal principal) {
        var result =  messageService.findSummaries(UUID.fromString(principal.getName()), term);
        
        List<MessageSummaryDto> resultDto = new ArrayList<>(result.size());
        result.forEach(r -> {
            var dto = MapperUtil.messageSummaryDto(r);
            resultDto.add(dto);
        });
        return resultDto;
    }
}
