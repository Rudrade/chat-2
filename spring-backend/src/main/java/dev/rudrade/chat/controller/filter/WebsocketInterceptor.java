package dev.rudrade.chat.controller.filter;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import dev.rudrade.chat.util.JwtUtil;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebsocketInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public @Nullable Message<?> preSend(Message<?> message, MessageChannel channel) {
        var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT == accessor.getCommand()) {
            var token = accessor.getFirstNativeHeader("Authorization");
            if (token == null) {
                return null;
            }

            var user = jwtUtil.getUserByToken(token);
            if (user == null) {
                return null;
            }

            var userToken = new UsernamePasswordAuthenticationToken(user, null, List.of());
            accessor.setUser(userToken);
        }

        return message;
    }

    

}
