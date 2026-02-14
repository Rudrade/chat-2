package dev.rudrade.chat.service;

import dev.rudrade.chat.dto.UserDto;
import dev.rudrade.chat.dto.request.LoginRequest;
import dev.rudrade.chat.exception.InvalidAccessException;
import dev.rudrade.chat.exception.UserNotFoundException;
import dev.rudrade.chat.model.User;
import dev.rudrade.chat.repository.UserRepository;
import dev.rudrade.chat.util.MapperUtil;
import dev.rudrade.chat.util.ValidationUtil;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final ValidationUtil validator;
    private final BCryptPasswordEncoder passwordEncoder;

    public User findUser(LoginRequest request) {
        Objects.requireNonNull(request, "request must be provided to find user");

        // Validate input
        validator.validate(request);

        // Find user by username
        var user = repository.findByUsername(request.username());
        if (user.isEmpty())
            throw new UserNotFoundException();

        // Validate if user is active and password matches
        if (!user.get().isActive() || !passwordEncoder.matches(request.password(), user.get().getPassword()))
            throw new UserNotFoundException();

        // Return user if all is true
        return user.get();
    }

    public Optional<User> findActiveById(UUID userId) {
        Objects.requireNonNull(userId, "userId must be provided to find user");
        var result = repository.findById(userId);
        if (result.isPresent() && !result.get().isActive()) {
            return Optional.empty();
        }

        return result;
    }

    public UserDto findDetails() {
        var user = repository.findDetails().orElseThrow(InvalidAccessException::new);
        return MapperUtil.userDto(user);
    }

    public List<User> findActiveByChat(UUID chatId) {
        Objects.requireNonNull(chatId, "id must exist to get users");

        return repository.findActiveByChatId(chatId);
    }
}
