package dev.rudrade.chat.controller.filter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import dev.rudrade.chat.service.UserService;
import dev.rudrade.chat.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Check if has bearer token
        var bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearerToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check if token is valid
        var decodedToken = jwtUtil.decodeToken(bearerToken);
        if (decodedToken == null) {
            response.setStatus(401);
            return;
        }

        // Check if user is active
        var userId = UUID.fromString(decodedToken.getSubject());
        var user = userService.findById(userId);
        if (user.isEmpty() || !user.get().isActive()) {
            response.setStatus(401);
            return;
        }

        // Add user to the SecurityContext
        // TODO: Test this context, to check if is clear upon request is done (create test with 2 users)
        var contextToken = new UsernamePasswordAuthenticationToken(user, null, List.of());
        contextToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(contextToken);

        filterChain.doFilter(request, response);
    }
    
}
