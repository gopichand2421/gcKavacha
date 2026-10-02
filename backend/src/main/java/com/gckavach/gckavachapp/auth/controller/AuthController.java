package com.gckavach.gckavachapp.auth.controller;

import com.gckavach.gckavachapp.auth.dto.CurrentUserResponse;
import com.gckavach.gckavachapp.auth.dto.LoginRequest;
import com.gckavach.gckavachapp.auth.dto.LoginResponse;
import com.gckavach.gckavachapp.auth.dto.RegisterRequest;
import com.gckavach.gckavachapp.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Register a new user.
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @Valid @RequestBody RegisterRequest request) {

        authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    /**
     * Authenticate an existing user and return JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> getCurrentUser(
            Authentication authentication) {

        String userId = authentication.getName();

        CurrentUserResponse response =
                authService.getCurrentUser(userId);

        return ResponseEntity.ok(response);
    }
}
