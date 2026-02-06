package dev.rudrade.chat.service;

import dev.rudrade.chat.dto.request.LoginRequest;
import dev.rudrade.chat.dto.response.LoginResponse;
import dev.rudrade.chat.exception.InvalidAccessException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;

    public LoginResponse authenticate(@NotNull LoginRequest request) {
        // Find user
        var user = userService.findUser(request);
        if (user == null) throw new InvalidAccessException(InvalidAccessException.USER_NOT_FOUND);

        // Generate and return JWT token
//TODO: IMPL
        return null;
    }
}
