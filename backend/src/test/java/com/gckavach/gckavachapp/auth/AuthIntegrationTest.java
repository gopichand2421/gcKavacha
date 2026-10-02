package com.gckavach.gckavachapp.auth;

import com.gckavach.gckavachapp.user.domain.Role;
import com.gckavach.gckavachapp.user.domain.User;
import com.gckavach.gckavachapp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;



import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterUser() throws Exception {

        String request = """
                {
                  "username": "integration.user",
                  "email": "integration@example.com",
                  "password": "Password@123",
                  "firstName": "Integration",
                  "lastName": "User"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated());
    }

    @Test
    void shouldLoginAndReturnJwt() throws Exception {

        createUser(
                "login.user",
                "login@example.com",
                "Password@123",
                Role.USER
        );

        String request = """
            {
              "username": "login.user",
              "password": "Password@123"
            }
            """;

        String response = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = extractToken(response);

        org.junit.jupiter.api.Assertions.assertNotNull(token);
        org.junit.jupiter.api.Assertions.assertFalse(token.isBlank());
    }

    @Disabled
    @Test
    void shouldRejectInvalidLogin() throws Exception {

        createUser(
                "invalid.login",
                "invalid@example.com",
                "Password@123",
                Role.USER
        );

        String request = """
                {
                  "username": "invalid.login",
                  "password": "WrongPassword"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().is4xxClientError());
    }

    @Disabled
    @Test
    void shouldRejectMeWithoutToken() throws Exception {

        mockMvc.perform(
                        get("/api/auth/me")
                )
                .andExpect(status().isUnauthorized());
    }


    @Test
    void shouldAccessMeWithValidToken() throws Exception {

        createUser(
                "me.user",
                "me@example.com",
                "Password@123",
                Role.USER
        );

        String token = loginAndGetToken(
                "me.user",
                "Password@123"
        );

        mockMvc.perform(
                        get("/api/auth/me")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowUserEndpointForUser() throws Exception {

        createUser(
                "user.endpoint",
                "user.endpoint@example.com",
                "Password@123",
                Role.USER
        );

        String token = loginAndGetToken(
                "user.endpoint",
                "Password@123"
        );

        mockMvc.perform(
                        get("/api/user/test")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectUserFromAdminEndpoint() throws Exception {

        createUser(
                "normal.user",
                "normal@example.com",
                "Password@123",
                Role.USER
        );

        String token = loginAndGetToken(
                "normal.user",
                "Password@123"
        );

        mockMvc.perform(
                        get("/api/admin/test")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminEndpointForAdmin() throws Exception {

        createUser(
                "admin.user",
                "admin@example.com",
                "Password@123",
                Role.ADMIN
        );

        String token = loginAndGetToken(
                "admin.user",
                "Password@123"
        );

        mockMvc.perform(
                        get("/api/admin/test")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectInvalidJwt() throws Exception {

        mockMvc.perform(
                        get("/api/auth/me")
                                .header(
                                        "Authorization",
                                        "Bearer invalid.jwt.token"
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    private User createUser(
            String username,
            String email,
            String password,
            Role role
    ) {

        User user = new User(
                username,
                email,
                passwordEncoder.encode(password),
                "Test",
                "User"
        );

        if (role == Role.ADMIN) {
            user.addRole(Role.ADMIN);
        }

        return userRepository.save(user);
    }

    private String loginAndGetToken(
            String username,
            String password
    ) throws Exception {

        String request = """
            {
              "username": "%s",
              "password": "%s"
            }
            """.formatted(username, password);

        String response = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return extractToken(response);
    }

    private String extractToken(String response) {

        String marker = "\"token\":\"";

        int start = response.indexOf(marker);

        if (start < 0) {
            throw new AssertionError(
                    "Token not found in login response: " + response
            );
        }

        start += marker.length();

        int end = response.indexOf("\"", start);

        if (end < 0) {
            throw new AssertionError(
                    "Invalid login response: " + response
            );
        }

        return response.substring(start, end);
    }

}
