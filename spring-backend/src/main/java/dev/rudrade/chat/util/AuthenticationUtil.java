package dev.rudrade.chat.util;

import java.security.Principal;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import dev.rudrade.chat.exception.InvalidAccessException;
import dev.rudrade.chat.model.User;

public class AuthenticationUtil {

    private AuthenticationUtil() {}

    public static User extractFromPrincipal(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken casted &&
            casted.getPrincipal() instanceof User user) {
                return user;
        }

        throw new InvalidAccessException();
    }
}