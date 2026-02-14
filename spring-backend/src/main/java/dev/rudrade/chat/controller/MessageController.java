package dev.rudrade.chat.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;

import dev.rudrade.chat.dto.MessageInputDto;
import dev.rudrade.chat.dto.MessageSummaryDto;
import dev.rudrade.chat.service.MessageService;
import dev.rudrade.chat.service.UserService;
import dev.rudrade.chat.util.AuthenticationUtil;
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
    private final UserService userService;

    @MessageMapping("/sendMessage")
    public void send(MessageInputDto input, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        log.debug("Start sendMessage");
        var user = AuthenticationUtil.extractFromPrincipal(principal);
        var message = messageService.sendMessage(input, user);
        
        // Send to all users of the chat that are subscribed
        var chatUsers = userService.findActiveByChat(message.idChatTo());
        if (!chatUsers.isEmpty()) {
            chatUsers.forEach(u -> {
                var subscriber = registry.getUser(u.getUsername());
                log.debug("user is subscribed:"+u.getId());
                if (subscriber != null) {
                    log.debug("starting to send to:"+u.getId());
                    messagingTemplate.convertAndSendToUser(u.getUsername(), "/topic/messages", message);
                    log.debug("done sent to "+u.getId());
                }
            });
        }
        log.debug("End sendMessage");
    }

    @MessageMapping("/search/{term}")
    public void findSummaries(@DestinationVariable String term, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        var user = AuthenticationUtil.extractFromPrincipal(principal);
        var result =  messageService.findSummaries(user.getId(), term);
        
        List<MessageSummaryDto> resultDto = new ArrayList<>(result.size());
        result.forEach(r -> {
            var dto = MapperUtil.messageSummaryDto(r);
            resultDto.add(dto);
        });

        // Send to the session that called
        var headers = SimpMessageHeaderAccessor.create();
        headers.setSessionId(headerAccessor.getSessionId());
        headers.setLeaveMutable(true);

        messagingTemplate.convertAndSendToUser(
            principal.getName(),
            "/topic/summaries",
            resultDto,
            headers.getMessageHeaders());
    }

    // TODO: Create GET to fetch latest summary that frontend calls at load.
}
