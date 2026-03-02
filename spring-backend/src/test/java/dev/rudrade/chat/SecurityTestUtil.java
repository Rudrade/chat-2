package dev.rudrade.chat;

import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import dev.rudrade.chat.model.User;

public class SecurityTestUtil {

    private final User user;

    private SecurityTestUtil(User user) {
        this.user = user;
    }

    public void authenticate() {
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
        SecurityContextHolder.setContext(context);
    }
    
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID userId;
        private String username;

        public Builder withId(String id) {
            this.userId = UUID.fromString(id);
            return this;
        }

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public SecurityTestUtil build() {
            var user = new User();
            user.setId(userId);
            user.setUsername(username);
            return new SecurityTestUtil(user);
        }
    }

}
