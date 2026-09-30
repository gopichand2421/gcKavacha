package com.gckavach.gckavachapp.auth.dto;

import com.gckavach.gckavachapp.user.domain.Role;
import com.gckavach.gckavachapp.user.domain.UserStatus;

import java.time.Instant;
import java.util.Set;

public record CurrentUserResponse(
        String id,
        String username,
        String email,
        String firstName,
        String lastName,
        Set<Role> roles,
        UserStatus status,
        Instant createdAt
) {
}

