package dev.rudrade.chat.controller;

import dev.rudrade.chat.dto.request.LoginRequest;
import dev.rudrade.chat.dto.response.LoginResponse;
import dev.rudrade.chat.service.AuthenticationService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController("auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService service;

    @PostMapping("login")
    public LoginResponse login(@NotNull LoginRequest request) {
        try {
            Thread.sleep(Duration.ofSeconds(1));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return service.authenticate(request);
    }
}
