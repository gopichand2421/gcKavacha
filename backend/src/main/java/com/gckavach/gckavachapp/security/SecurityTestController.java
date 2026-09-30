package com.gckavach.gckavachapp.security;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SecurityTestController {

    @GetMapping("/user/test")
    public String userEndpoint(Authentication authentication) {
        return "USER access granted for: "
                + authentication.getName();
    }

    @GetMapping("/admin/test")
    public String adminEndpoint(Authentication authentication) {
        return "ADMIN access granted for: "
                + authentication.getName();
    }
}
