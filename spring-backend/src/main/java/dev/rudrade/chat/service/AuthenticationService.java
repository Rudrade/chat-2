package dev.rudrade.chat.service;

import dev.rudrade.chat.dto.request.LoginRequest;
import dev.rudrade.chat.dto.response.LoginResponse;
import dev.rudrade.chat.exception.UserNotFoundException;
import dev.rudrade.chat.util.JwtUtil;
import dev.rudrade.chat.util.ValidationUtil;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final ValidationUtil validator;
    private final JwtUtil jwtUtil;

    public LoginResponse authenticate(@NotNull LoginRequest request) {
        // Validate input
        validator.validate(request);

        // Find user
        var user = userService.findUser(request);
        if (user == null) throw new UserNotFoundException();

        // Generate and return JWT token
        var token = jwtUtil.generateToken(user);
        return new LoginResponse(token);
    }
}
