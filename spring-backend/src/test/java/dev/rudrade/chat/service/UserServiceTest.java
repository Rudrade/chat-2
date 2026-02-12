package dev.rudrade.chat.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import dev.rudrade.chat.dto.request.LoginRequest;

import dev.rudrade.chat.exception.InvalidAccessException;
import dev.rudrade.chat.exception.InvalidDataException;
import dev.rudrade.chat.exception.UserNotFoundException;
import dev.rudrade.chat.model.User;
import dev.rudrade.chat.repository.UserRepository;
import dev.rudrade.chat.util.ValidationUtil;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ValidationUtil validationUtil;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    private UserService target;

    @BeforeEach
    void init() {
        target = new UserService(userRepository, validationUtil, passwordEncoder);
    }

    //=============
    //  findUser
    //=============

    @Test
    void itShouldThrowUserNotFoundWhenPasswordMismatch_findUser() {
        var request = new LoginRequest("test", "test");

        var user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("test");
        user.setActive(true);
        user.setPassword("encrypt-test");

        when(userRepository.findByUsername("test"))
            .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("test", "encrypt-test"))
            .thenReturn(Boolean.FALSE);

        assertThrows(UserNotFoundException.class, 
            () -> target.findUser(request)
        );

        verify(validationUtil, times(1)).validate(request);
        verify(userRepository, times(1)).findByUsername("test");
        verify(passwordEncoder, times(1)).matches("test", "encrypt-test");
        verifyNoMoreInteractions(userRepository, validationUtil, passwordEncoder);
    
    }

    @Test
    void itShouldThrowUserNotFoundWhenUserIsInactive_findUser() {
        var request = new LoginRequest("test", "test");

        var user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("test");
        user.setActive(false);

        when(userRepository.findByUsername("test"))
            .thenReturn(Optional.of(user));

        assertThrows(UserNotFoundException.class, 
            () -> target.findUser(request)
        );

        verify(validationUtil, times(1)).validate(request);
        verify(userRepository, times(1)).findByUsername("test");
        verifyNoMoreInteractions(userRepository, validationUtil, passwordEncoder);
    }

    @Test
    void itShouldThrowUserNotFoundWhenUserIsEmpty_findUser() {
        var request = new LoginRequest("test", "test");

        when(userRepository.findByUsername("test"))
            .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, 
            () -> target.findUser(request)
        );

        verify(validationUtil, times(1)).validate(request);
        verify(userRepository, times(1)).findByUsername("test");
        verifyNoMoreInteractions(userRepository, validationUtil, passwordEncoder);
    }

    @Test
    void itShouldThrowWhenValidatorFails_findUser() {
        var request = new LoginRequest("test", "test");

        doThrow(InvalidDataException.class)
            .when(validationUtil)
            .validate(request);

        assertThrows(InvalidDataException.class, 
            () -> target.findUser(request)
        );

        verify(validationUtil, times(1)).validate(request);
        verifyNoMoreInteractions(validationUtil);
        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void itShouldFindUser() {
        var request = new LoginRequest("test", "test");

        var user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("test");
        user.setPassword("encrypt-test");
        user.setActive(true);

        when(userRepository.findByUsername("test"))
            .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("test", "encrypt-test"))
            .thenReturn(Boolean.TRUE);

        var result = target.findUser(request);

        assertThat(result)
            .isNotNull()
            .isEqualTo(user);

        verify(userRepository, times(1)).findByUsername("test");
        verify(validationUtil, times(1)).validate(request);
        verify(passwordEncoder, times(1)).matches("test", "encrypt-test");
        verifyNoMoreInteractions(userRepository, validationUtil, passwordEncoder);
    }

    //================
    //  findDetails
    //================

    @Test
    void itShouldThrowWhenDetailsNotFound() {

        when(userRepository.findDetails())
            .thenReturn(Optional.empty());

        assertThrows(InvalidAccessException.class, 
            () -> target.findDetails()
        );

        verify(userRepository, times(1)).findDetails();
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(validationUtil, passwordEncoder);
    }

    @Test
    void itShouldFindDetails() {
        var user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("test");
        user.setName("name of test");

        when(userRepository.findDetails())
            .thenReturn(Optional.of(user));

        var result = target.findDetails();
        assertThat(result)
            .isNotNull()
            .satisfies(r -> {
                assertThat(r.id()).isEqualTo(user.getId());
                assertThat(r.username()).isEqualTo(user.getUsername());
                assertThat(r.name()).isEqualTo(user.getName());
            });

        verify(userRepository, times(1)).findDetails();
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(validationUtil, passwordEncoder);
    }

    //=============
    //  findById
    //=============

    @Test
    void itShouldFindById() {
        var id = UUID.randomUUID();

        var user = new User();
        user.setId(id);

        when(userRepository.findById(id))
            .thenReturn(Optional.of(user));

        var result = target.findById(id);

        assertThat(result)
            .isNotNull()
            .isPresent()
            .get().isEqualTo(user);

        verify(userRepository, times(1)).findById(id);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(validationUtil, passwordEncoder);
    }

}
