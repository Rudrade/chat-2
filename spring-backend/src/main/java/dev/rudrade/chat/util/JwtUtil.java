package dev.rudrade.chat.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import dev.rudrade.chat.exception.InvalidDataException;
import dev.rudrade.chat.model.User;
import dev.rudrade.chat.service.UserService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final UserService userService;

    @Value("${app.security.jwt.secret}")
    private String secret;

    @Value("${app.security.jwt.issuer}")
    private String issuer;

    public User getUserByToken(String token) {
        if (token == null)
            throw new InvalidDataException("token must be provided");

        // Check if token is valid
        var decodedToken = decodeToken(token);
        if (decodedToken == null) {
            throw new InvalidDataException("token must be valid");
        }

        // Check if user is active
        var userId = UUID.fromString(decodedToken.getSubject());
        var user = userService.findActiveById(userId);
        if (user.isPresent() && user.get().isActive()) {
            return user.get();
        }

        return null;
    }

    public String generateToken(User user) {
        Objects.requireNonNull(user, "user must exist to generate a token");

        return JWT.create()
            .withIssuer(issuer)
            .withSubject(user.getId().toString())
            .withIssuedAt(Instant.now())
            .withExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
            .sign(getAlgorithm());
    }

    public DecodedJWT decodeToken(String token) {
        if (token == null || token.isBlank())
            throw new InvalidDataException("token must be provided");

        try {
            var newToken = token;
            if (newToken.startsWith("Bearer ")) {
                newToken = newToken.substring(7);
            }

            // Return if is valid
            return JWT.require(getAlgorithm())
                .withIssuer(issuer)
                .build()
                .verify(newToken);

        } catch(JWTVerificationException ex) {
            throw new InvalidDataException("token provided invalid");
        }
    }

    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(secret);
    }

}
