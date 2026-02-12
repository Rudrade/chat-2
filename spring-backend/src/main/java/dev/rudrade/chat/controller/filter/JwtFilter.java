package dev.rudrade.chat.controller.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import dev.rudrade.chat.exception.InvalidDataException;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            // Check if has bearer token
            var bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (bearerToken == null) {
                filterChain.doFilter(request, response);
                return;
            }

            var user = jwtUtil.getUserByToken(bearerToken);
            if (user == null) {
                response.setStatus(401);
                return;
            }

            // Add user to the SecurityContext
            var contextToken = new UsernamePasswordAuthenticationToken(user, null, List.of());
            contextToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(contextToken);

            filterChain.doFilter(request, response);
        } catch (InvalidDataException ex) {
            response.setStatus(401);
        }
    }
    
}
