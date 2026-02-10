package dev.rudrade.chat.controller.filter;

import org.jspecify.annotations.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebsocketInterceptor implements ChannelInterceptor {

    @Override
    public @Nullable Message<?> preSend(Message<?> message, MessageChannel channel) {
        var acessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (acessor != null && StompCommand.CONNECT.equals(acessor.getCommand())) {
            var userId = acessor.getFirstNativeHeader("userId");//TODO: Replace with JWT and validate
            log.trace("Adding user:"+userId);
            acessor.setUser(() -> userId == null ? "unkown" : userId);
        }
        return message;
    }

}
