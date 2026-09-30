package com.gckavach.gckavachapp.user.service;

import com.gckavach.gckavachapp.user.domain.Role;
import com.gckavach.gckavachapp.user.domain.User;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserRoleService {

    public Set<Role> getRoles(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        return Set.copyOf(user.getRoles());
    }

    public boolean hasRole(User user, Role role) {
        if (user == null) {
            return false;
        }

        if (role == null) {
            return false;
        }

        return user.getRoles().contains(role);
    }

    public void addRole(User user, Role role) {

        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        user.addRole(role);
    }

    public void removeRole(User user, Role role) {
        validateUser(user);
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        } /* * A user must always have at least one role. */
        if (user.getRoles().size() == 1 && user.hasRole(role)) {
            throw new IllegalStateException("User must have at least one role");
        }
        user.removeRole(role);
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
    }
}
