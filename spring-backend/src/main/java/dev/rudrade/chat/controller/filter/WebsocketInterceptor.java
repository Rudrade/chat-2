package dev.rudrade.chat.controller.filter;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import dev.rudrade.chat.exception.InvalidAccessException;
import dev.rudrade.chat.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebsocketInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public @Nullable Message<?> preSend(Message<?> message, MessageChannel channel) {
        var acessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (acessor != null && StompCommand.CONNECT.equals(acessor.getCommand())) {
            var authHeader = acessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
            log.trace("authHeader:"+authHeader);
            var user = jwtUtil.getUserByToken(authHeader);
            log.trace("user:"+user);
            if (user != null) {
                var token = new UsernamePasswordAuthenticationToken(user, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(token);
                acessor.setUser(token);

            } else {
                throw new InvalidAccessException();
            }

        }
        return message;
    }

}
