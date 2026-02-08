package dev.rudrade.chat.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import dev.rudrade.chat.model.User;
import jakarta.validation.constraints.NotNull;

@Component
public class JwtUtil {

    @Value("${app.security.jwt.secret}")
    private String secret;

    @Value("${app.security.jwt.issuer}")
    private String issuer;

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
