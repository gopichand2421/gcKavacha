package com.gckavach.gckavachapp.auth.service;

import com.gckavach.gckavachapp.auth.dto.CurrentUserResponse;
import com.gckavach.gckavachapp.auth.dto.LoginRequest;
import com.gckavach.gckavachapp.auth.dto.LoginResponse;
import com.gckavach.gckavachapp.auth.dto.RegisterRequest;
import com.gckavach.gckavachapp.user.domain.User;
import com.gckavach.gckavachapp.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordService passwordService,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException(
                    "Username already exists");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                    "Email already exists");
        }

        String passwordHash =
                passwordService.hash(request.password());

        User user = new User(
                request.username(),
                request.email(),
                passwordHash,
                request.firstName(),
                request.lastName()
        );

        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository
                .findByUsername(request.username())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid username or password"));

        if (!passwordService.matches(
                request.password(),
                user.getPasswordHash())) {

            throw new IllegalArgumentException(
                    "Invalid username or password");
        }

        if (user.getStatus() !=
                com.gckavach.gckavachapp.user.domain.UserStatus.ACTIVE) {

            throw new IllegalStateException(
                    "User account is not active");
        }

        String token = jwtService.generateToken(
                user.getId(),
                user.getRoles().stream().toList()
        );

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpiration()
        );
    }

    public CurrentUserResponse getCurrentUser(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        return new CurrentUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoles(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}
