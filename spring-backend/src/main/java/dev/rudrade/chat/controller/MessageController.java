package dev.rudrade.chat.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;

import dev.rudrade.chat.dto.MessageInputDto;
import dev.rudrade.chat.dto.request.ChatMessageFilter;
import dev.rudrade.chat.dto.request.MessageSearchFilter;
import dev.rudrade.chat.dto.response.MessageResponse;
import dev.rudrade.chat.service.MessageService;
import dev.rudrade.chat.service.UserService;
import dev.rudrade.chat.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private static final String TOPIC_SUMMARIES = "/topic/summaries";
    private static final String TOPIC_MESSAGES = "/topic/messages";

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
                    messagingTemplate.convertAndSendToUser(u.getUsername(), TOPIC_MESSAGES, new MessageResponse(List.of(message), 1l));
                    log.debug("done sent to "+u.getId());
                }
            });
        }
        log.debug("End sendMessage");
    }

    @MessageMapping("/messages")
    public void findMessages(ChatMessageFilter filter, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        var user = AuthenticationUtil.extractFromPrincipal(principal);
        var result = messageService.findMessages(filter, user.getId());

        sendToSession(principal, headerAccessor, TOPIC_MESSAGES, result);
    }

    @MessageMapping("/search")
    public void findSummaries(MessageSearchFilter filter, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        var user = AuthenticationUtil.extractFromPrincipal(principal);
        var result =  messageService.findSummaries(filter, user.getId());

        sendToSession(principal, headerAccessor, TOPIC_SUMMARIES, result);
    }

    private void sendToSession(Principal principal, SimpMessageHeaderAccessor headerAccessor, String destination, Object payload) {
        var headers = SimpMessageHeaderAccessor.create();
        headers.setSessionId(headerAccessor.getSessionId());
        headers.setLeaveMutable(true);

        messagingTemplate.convertAndSendToUser(
            principal.getName(),
            destination,
            payload,
            headers.getMessageHeaders());
    }

}
