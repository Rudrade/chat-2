package dev.rudrade.chat.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import dev.rudrade.chat.model.User;
import dev.rudrade.chat.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final UserService userService;

    @Value("${app.security.jwt.secret}")
    private String secret;

    @Value("${app.security.jwt.issuer}")
    private String issuer;

    public User getUserByToken(@NotNull String token) {
        // Check if token is valid
        var decodedToken = decodeToken(token);
        if (decodedToken == null) {
            return null;
        }

        // Check if user is active
        var userId = UUID.fromString(decodedToken.getSubject());
        var user = userService.findById(userId);
        if (user.isPresent() && user.get().isActive()) {
            return user.get();
        }

        return null;
    }

    public String generateToken(@NotNull User user) {
        return JWT.create()
            .withIssuer(issuer)
            .withSubject(user.getId().toString())
            .withIssuedAt(Instant.now())
            .withExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
            .sign(getAlgorithm());
    }

    public DecodedJWT decodeToken(@NotNull String token) {
        // Validate if has Bearer
        if (!token.startsWith("Bearer "))
            return null;

        try {
            var newToken = token.substring(7);

            // Return if is valid
            return JWT.require(getAlgorithm())
                .withIssuer(issuer)
                .build()
                .verify(newToken);

        } catch(JWTVerificationException ex) {
            return null;
        }
    }

    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(secret);
    }

}
