package dev.rudrade.chat.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import dev.rudrade.chat.exception.InvalidDataException;
import dev.rudrade.chat.model.User;
import dev.rudrade.chat.service.UserService;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @Mock private UserService userService;
    private JwtUtil target;

    @BeforeEach
    void init() {
        target = new JwtUtil(userService);
        ReflectionTestUtils.setField(target, "secret", "secret123");
        ReflectionTestUtils.setField(target, "issuer", "issuer456");
    }

    private String newToken(UUID subject) {
        return JWT.create()
            .withSubject(subject.toString())
            .withIssuer("issuer456")
            .sign(Algorithm.HMAC256("secret123"));
    }

    //===================
    //  getUserByToken
    //===================

    @Test
    void itShouldReturnNullIfUserIsInactive() {
        var userId = UUID.randomUUID();
        var token = newToken(userId);
        var user = new User();
        user.setId(userId);
        user.setActive(false);

        when(userService.findById(userId))
            .thenReturn(Optional.of(user));

        var result = target.getUserByToken(token);
        assertNull(result);

        verify(userService, times(1)).findById(userId);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void itShouldReturnNullIfUserDoesntExist() {
        var userId = UUID.randomUUID();
        var token = newToken(userId);

        when(userService.findById(userId))
            .thenReturn(Optional.empty());

        var result = target.getUserByToken(token);
        assertNull(result);

        verify(userService, times(1)).findById(userId);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void itShouldThrowWhenTokenIsNull() {
        assertThrows(InvalidDataException.class, 
            () -> target.getUserByToken(null)
        );
        verifyNoInteractions(userService);
    }

    @Test
    void itShouldGetUserByToken() {
        var userId = UUID.randomUUID();
        var token = newToken(userId);

        var user = new User();
        user.setId(userId);
        user.setUsername("test user");
        user.setActive(true);

        when(userService.findById(userId))
            .thenReturn(Optional.of(user));

        var result = target.getUserByToken(token);

        assertThat(result)
            .isNotNull();
        
        verify(userService, times(1)).findById(userId);
        verifyNoMoreInteractions(userService);
    }

    //==================
    //  generateToken
    //==================

    @Test
    void itShouldThrowWhenUserIsNull() {
        var result = assertThrows(NullPointerException.class, 
            () -> target.generateToken(null)
        ).getMessage();

        assertEquals("user must exist to generate a token", result);
    }

    @Test
    void itShouldGenerateToken() {
        var user = new User();
        user.setId(UUID.randomUUID());

        var token = target.generateToken(user);
        assertThat(token).isNotBlank();

        var decoded = target.decodeToken(token);
        assertEquals(user.getId().toString(), decoded.getSubject());
        assertNotNull(decoded.getIssuedAt());
        assertThat(decoded.getExpiresAt()).isNotNull().isInTheFuture();
    }

    //================
    //  decodeToken
    //================

    @ParameterizedTest
    @NullAndEmptySource
    void itShouldThrowWhenDecodeTokenIsBlank(String arg) {
        assertThrows(InvalidDataException.class, 
            () -> target.decodeToken(arg)
        );
    }

    @Test
    void itShouldThrowWhenIsInvalidToken() {
        
        assertThrows(InvalidDataException.class, 
            () -> target.decodeToken("Bearer random str")
        );

        verifyNoInteractions(userService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Bearer "})
    void itShouldDecodeToken(String prefix) {
        var subject = UUID.randomUUID();
        var token = newToken(subject);

        var result = target.decodeToken(prefix+token);

        assertThat(result)
            .isNotNull()
            .satisfies(arg -> {
                assertEquals(subject.toString(), arg.getSubject());
            });

        verifyNoInteractions(userService);
    }

}
