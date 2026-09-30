package com.gckavach.gckavachapp.security;

import com.gckavach.gckavachapp.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader(AUTHORIZATION_HEADER);

        // No JWT -> let Spring Security decide whether authentication
        // is required for this endpoint.
        if (authorizationHeader == null
                || !authorizationHeader.startsWith(BEARER_PREFIX)) {

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                authorizationHeader.substring(BEARER_PREFIX.length()).trim();

        if (token.isBlank()) {
            sendUnauthorized(response);
            return;
        }

        try {

            Jws<Claims> jwt =
                    jwtService.parseAndValidate(token);

            Claims claims = jwt.getPayload();

            String userId = claims.getSubject();

            if (userId == null || userId.isBlank()) {
                sendUnauthorized(response);
                return;
            }

            @SuppressWarnings("unchecked")
            List<String> roles =
                    claims.get("roles", List.class);

            if (roles == null) {
                roles = List.of();
            }

            List<SimpleGrantedAuthority> authorities =
                    roles.stream()
                            .map(String::valueOf)
                            .map(role -> new SimpleGrantedAuthority(
                                    "ROLE_" + role))
                            .toList();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            authorities
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (JwtException | IllegalArgumentException ex) {

            SecurityContextHolder.clearContext();

            sendUnauthorized(response);
        }
    }

    private void sendUnauthorized(HttpServletResponse response)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        response.getWriter().write("""
                {
                  "code": "INVALID_TOKEN",
                  "message": "Authentication token is invalid or expired"
                }
                """);
    }
}