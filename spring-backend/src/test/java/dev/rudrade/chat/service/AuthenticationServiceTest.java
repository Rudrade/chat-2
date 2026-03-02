package dev.rudrade.chat.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.rudrade.chat.dto.request.LoginRequest;
import dev.rudrade.chat.exception.UserNotFoundException;
import dev.rudrade.chat.model.User;
import dev.rudrade.chat.util.JwtUtil;
import dev.rudrade.chat.util.ValidationUtil;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    private AuthenticationService target;
    @Mock private UserService userService;
    @Mock private JwtUtil jwtUtil;
    @Mock private ValidationUtil validator;

    @BeforeEach
    void init() {
        target = new AuthenticationService(userService, validator, jwtUtil);
    }

    //=================
    //  authenticate
    //=================

    @Test
    void itShouldAuthenticate() {
        var request = new LoginRequest("test", "test");

        var user = new User();
        when(userService.findUser(request))
            .thenReturn(user);

        var token = "mocked-token";
        when(jwtUtil.generateToken(user))
            .thenReturn(token);

        var result = target.authenticate(request);
        assertThat(result)
            .isNotNull()
            .satisfies(r -> {
               assertThat(r.token())
                .isEqualTo(token);
            });

        verify(userService, times(1)).findUser(request);
        verify(validator, times(1)).validate(request);
        verify(jwtUtil, times(1)).generateToken(user);
        verifyNoMoreInteractions(userService, validator, jwtUtil);
    }

    @Test
    void itShouldThrowUserNotFoundException() {
        var request = new LoginRequest("test", "test");

        assertThrows(UserNotFoundException.class, 
            () -> target.authenticate(request)
        );

        verify(userService, times(1)).findUser(request);
        verify(validator, times(1)).validate(request);
        verifyNoMoreInteractions(userService, validator);
        verifyNoInteractions(jwtUtil);
    }

}
