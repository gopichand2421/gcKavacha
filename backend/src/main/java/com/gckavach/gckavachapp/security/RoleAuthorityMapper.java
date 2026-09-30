package com.gckavach.gckavachapp.security;

import com.gckavach.gckavachapp.user.domain.Role;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public final class RoleAuthorityMapper {

    private static final String ROLE_PREFIX = "ROLE_";

    private RoleAuthorityMapper() {
        // Utility class
    }

    public static Collection<SimpleGrantedAuthority> toAuthorities(
            Set<Role> roles
    ) {

        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }

        return roles.stream()
                .map(RoleAuthorityMapper::toAuthority)
                .collect(Collectors.toUnmodifiableSet());
    }

    public static SimpleGrantedAuthority toAuthority(Role role) {

        if (role == null) {
            throw new IllegalArgumentException(
                    "Role cannot be null"
            );
        }

        return new SimpleGrantedAuthority(
                ROLE_PREFIX + role.name()
        );
    }
}

