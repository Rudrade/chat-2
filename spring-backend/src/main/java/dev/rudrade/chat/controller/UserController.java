package dev.rudrade.chat.controller;

import dev.rudrade.chat.dto.UserDto;
import dev.rudrade.chat.dto.request.LoginRequest;
import dev.rudrade.chat.dto.response.LoginResponse;
import dev.rudrade.chat.service.AuthenticationService;
import dev.rudrade.chat.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        path = "/user",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class UserController {

    private final AuthenticationService service;
    private final UserService userService;

    @PostMapping(path = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(@NotNull @RequestBody LoginRequest request) {
        return service.authenticate(request);
    }

    @GetMapping("/details")
    @Operation(
        security = {@SecurityRequirement(name = "bearer")}
    )
    public UserDto getDetails() {
        return userService.findDetails();
    }
}
