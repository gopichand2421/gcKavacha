package com.gckavach.gckavachapp.alert;

import com.gckavach.gckavachapp.alert.repository.AlertRepository;
import com.gckavach.gckavachapp.user.domain.User;
import com.gckavach.gckavachapp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlertIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token;

    @BeforeEach
    void setUp() throws Exception {

        alertRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User(
                "alert-test-user",
                "alert-test@gckavacha.com",
                passwordEncoder.encode("Password123!"),
                "Alert",
                "Tester"
        );

        userRepository.save(user);

        String loginRequest = """
                {
                  "username": "alert-test-user",
                  "password": "Password123!"
                }
                """;

        String response = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        token = extractJsonValue(response, "token");
    }

    @Test
    void shouldCreateAlert() throws Exception {

        String request = """
                {
                  "externalAlertId": "prom-test-001",
                  "title": "High CPU Usage",
                  "description": "CPU exceeded 90 percent",
                  "source": "prometheus",
                  "severity": "HIGH",
                  "serviceName": "payment-service",
                  "environment": "production"
                }
                """;

        mockMvc.perform(
                        post("/api/alerts")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.externalAlertId")
                        .value("prom-test-001"))
                .andExpect(jsonPath("$.title")
                        .value("High CPU Usage"))
                .andExpect(jsonPath("$.severity")
                        .value("HIGH"))
                .andExpect(jsonPath("$.status")
                        .value("OPEN"))
                .andExpect(jsonPath("$.fingerprint")
                        .isNotEmpty());
    }

    @Test
    void shouldQueryAlerts() throws Exception {

        createTestAlert();

        mockMvc.perform(
                        get("/api/alerts")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .param("status", "OPEN")
                                .param("page", "0")
                                .param("size", "20")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()")
                        .value(1))
                .andExpect(jsonPath("$.content[0].status")
                        .value("OPEN"))
                .andExpect(jsonPath("$.totalElements")
                        .value(1));
    }

    @Test
    void shouldAcknowledgeAlert() throws Exception {

        String alertId = createTestAlert();

        mockMvc.perform(
                        patch(
                                "/api/alerts/"
                                        + alertId
                                        + "/acknowledge"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("ACKNOWLEDGED"))
                .andExpect(jsonPath("$.acknowledgedAt")
                        .isNotEmpty());
    }

    @Test
    void shouldResolveAlert() throws Exception {

        String alertId = createTestAlert();

        mockMvc.perform(
                        patch(
                                "/api/alerts/"
                                        + alertId
                                        + "/resolve"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("RESOLVED"))
                .andExpect(jsonPath("$.resolvedAt")
                        .isNotEmpty());
    }

    @Test
    void shouldNotCreateDuplicateActiveAlert() throws Exception {

        String request = """
                {
                  "externalAlertId": "prom-duplicate-001",
                  "title": "Database Connection Failure",
                  "description": "Database unavailable",
                  "source": "prometheus",
                  "severity": "CRITICAL",
                  "serviceName": "payment-service",
                  "environment": "production"
                }
                """;

        mockMvc.perform(
                        post("/api/alerts")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/alerts")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code")
                        .value("ALERT_CONFLICT"));
    }

    @Test
    void shouldReturn404ForUnknownAlert() throws Exception {

        mockMvc.perform(
                        patch("/api/alerts/unknown-alert/resolve")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code")
                        .value("ALERT_NOT_FOUND"));
    }

    @Test
    void shouldRejectInvalidLifecycleTransition() throws Exception {

        String alertId = createTestAlert();

        mockMvc.perform(
                        patch(
                                "/api/alerts/"
                                        + alertId
                                        + "/resolve"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        patch(
                                "/api/alerts/"
                                        + alertId
                                        + "/acknowledge"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code")
                        .value("ALERT_CONFLICT"));
    }

    @Test
    void shouldRejectInvalidAlertRequest() throws Exception {

        String invalidRequest = """
                {
                  "externalAlertId": "",
                  "title": "",
                  "source": "",
                  "severity": null,
                  "serviceName": "",
                  "environment": ""
                }
                """;

        mockMvc.perform(
                        post("/api/alerts")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidRequest)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {

        mockMvc.perform(
                        get("/api/alerts")
                )
                .andExpect(status().isUnauthorized());
    }

    private String createTestAlert() throws Exception {

        String request = """
                {
                  "externalAlertId": "prom-helper-001",
                  "title": "High Memory Usage",
                  "description": "Memory exceeded threshold",
                  "source": "prometheus",
                  "severity": "HIGH",
                  "serviceName": "order-service",
                  "environment": "production"
                }
                """;

        String response = mockMvc.perform(
                        post("/api/alerts")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return extractJsonValue(response, "id");
    }

    /**
     * Extracts a simple String field from a JSON response.
     *
     * This intentionally avoids requiring an ObjectMapper bean
     * in the Spring test context.
     */
    private String extractJsonValue(
            String json,
            String fieldName
    ) {
        String search = "\"" + fieldName + "\":";

        int fieldStart = json.indexOf(search);

        if (fieldStart == -1) {
            throw new AssertionError(
                    "Field '" + fieldName
                            + "' not found in response: "
                            + json
            );
        }

        int valueStart = fieldStart + search.length();

        while (valueStart < json.length()
                && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        if (valueStart >= json.length()
                || json.charAt(valueStart) != '"') {
            throw new AssertionError(
                    "Field '" + fieldName
                            + "' is not a String: "
                            + json
            );
        }

        int valueEnd = json.indexOf(
                '"',
                valueStart + 1
        );

        if (valueEnd == -1) {
            throw new AssertionError(
                    "Invalid JSON response: " + json
            );
        }

        return json.substring(
                valueStart + 1,
                valueEnd
        );
    }
}

