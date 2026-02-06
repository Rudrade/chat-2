package dev.rudrade.chat.service;

import dev.rudrade.chat.dto.request.LoginRequest;
import dev.rudrade.chat.exception.InvalidAccessException;
import dev.rudrade.chat.exception.InvalidDataException;
import dev.rudrade.chat.model.User;
import dev.rudrade.chat.repository.UserRepository;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final Validator validator;

    public User findUser(@NotNull LoginRequest request) {
        // Validate input
        var result = validator.validate(request);
        if (!result.isEmpty())
            throw new InvalidDataException(result);

        // Find user by username
        var user = repository.findByUsername(request.username());
        if (user.isEmpty())
            throw new InvalidAccessException(InvalidAccessException.USER_NOT_FOUND);

        // Validate if password matches
        //TODO: Encrypt
        if (!user.get().isActive() || !request.password().equals(user.get().getPassword()))
            throw new InvalidAccessException(InvalidAccessException.USER_NOT_FOUND);

        // Return user if all is true
        return user.get();
    }
}
