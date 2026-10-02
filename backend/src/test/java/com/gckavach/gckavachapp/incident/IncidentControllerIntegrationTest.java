package com.gckavach.gckavachapp.incident;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gckavach.gckavachapp.alert.event.AlertEvent;
import com.gckavach.gckavachapp.incident.domain.Incident;
import com.gckavach.gckavachapp.incident.domain.IncidentSeverity;
import com.gckavach.gckavachapp.incident.domain.IncidentStatus;
import com.gckavach.gckavachapp.incident.event.IncidentEvent;
import com.gckavach.gckavachapp.incident.repository.IncidentRepository;
import com.gckavach.gckavachapp.user.domain.Role;
import com.gckavach.gckavachapp.user.domain.User;
import com.gckavach.gckavachapp.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Integration tests for IncidentController.
 *
 * Kafka is mocked/disabled because these HTTP integration tests
 * should not require a running Kafka broker.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {

        /*
         * JWT configuration
         */
        "JWT_SECRET=gckavacha-test-jwt-secret-key-2026-minimum-32-bytes",
        "JWT_EXPIRATION=3600",

        /*
         * IMPORTANT:
         *
         * Do not start Kafka listeners during controller tests.
         *
         * KafkaTemplate is mocked below, so producer calls do not
         * require a Kafka broker either.
         */
        "spring.kafka.listener.auto-startup=false"
})
@AutoConfigureMockMvc
class IncidentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /*
     * IMPORTANT:
     *
     * Do NOT use:
     *
     * @Autowired
     * private ObjectMapper objectMapper;
     *
     * Your current test context does not expose ObjectMapper
     * as a bean.
     */
    private final ObjectMapper objectMapper =
            new ObjectMapper().findAndRegisterModules();

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    /**
     * Kafka publisher used by IncidentService.
     *
     * MockitoBean replaces the real KafkaTemplate in the
     * Spring application context.
     */
    @MockitoBean
    private KafkaTemplate<String, IncidentEvent> incidentKafkaTemplate;


    /**
     * Alert functionality is part of the complete Spring context.
     *
     * Mock it as well so no real Kafka producer is required.
     */
    @MockitoBean
    private KafkaTemplate<String, AlertEvent> alertKafkaTemplate;


    private String adminUsername;

    private String adminPassword;


    @BeforeEach
    void setUp() {

        /*
         * Clean incidents before every test.
         */
        incidentRepository.deleteAll();

        /*
         * Use unique credentials for every test.
         */
        adminUsername =
                "incident-admin-" + UUID.randomUUID();

        adminPassword =
                "Admin@12345";


        User adminUser = new User();

        adminUser.setUsername(adminUsername);

        adminUser.setEmail(
                adminUsername + "@gckavacha.local"
        );

        adminUser.setPasswordHash(
                passwordEncoder.encode(adminPassword)
        );

        adminUser.setFirstName("Incident");

        adminUser.setLastName("Admin");

        adminUser.setRoles(
                Set.of(Role.ADMIN)
        );

        userRepository.save(adminUser);
    }


    // =========================================================================
    // CREATE INCIDENT
    // =========================================================================

    @Test
    void shouldCreateIncident() throws Exception {

        String token =
                loginAndGetToken(
                        adminUsername,
                        adminPassword
                );

        String incidentNumber =
                "INC-" + UUID.randomUUID();


        String request = """
                {
                    "incidentNumber": "%s",
                    "title": "Payment service unavailable",
                    "description": "Payment API is returning HTTP 503 errors",
                    "severity": "CRITICAL",
                    "serviceName": "payment-service",
                    "environment": "production"
                }
                """.formatted(incidentNumber);


        MvcResult result =
                mockMvc.perform(
                                post("/api/incidents")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(request)
                        )
                        .andExpect(status().isCreated())
                        .andReturn();


        JsonNode response =
                objectMapper.readTree(
                        result.getResponse()
                                .getContentAsString()
                );


        assertThat(response.get("id"))
                .isNotNull();

        assertThat(response.get("incidentNumber").asText())
                .isEqualTo(incidentNumber);

        assertThat(response.get("title").asText())
                .isEqualTo(
                        "Payment service unavailable"
                );

        assertThat(response.get("severity").asText())
                .isEqualTo("CRITICAL");

        assertThat(response.get("status").asText())
                .isEqualTo("OPEN");

        assertThat(response.get("serviceName").asText())
                .isEqualTo("payment-service");

        assertThat(response.get("environment").asText())
                .isEqualTo("production");


        Incident savedIncident =
                incidentRepository
                        .findByIncidentNumber(incidentNumber)
                        .orElseThrow();


        assertThat(savedIncident.getTitle())
                .isEqualTo(
                        "Payment service unavailable"
                );

        assertThat(savedIncident.getStatus())
                .isEqualTo(
                        IncidentStatus.OPEN
                );

        assertThat(savedIncident.getSeverity())
                .isEqualTo(
                        IncidentSeverity.CRITICAL
                );

        assertThat(savedIncident.getDetectedAt())
                .isNotNull();
    }


    // =========================================================================
    // GET INCIDENT
    // =========================================================================

    @Test
    void shouldGetIncidentById() throws Exception {

        String token =
                loginAndGetToken(
                        adminUsername,
                        adminPassword
                );


        Incident incident =
                createIncident(
                        "Database connection failures",
                        IncidentSeverity.HIGH
                );


        MvcResult result =
                mockMvc.perform(
                                get(
                                        "/api/incidents/{incidentId}",
                                        incident.getId()
                                )
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                        )
                        .andExpect(status().isOk())
                        .andReturn();


        JsonNode response =
                objectMapper.readTree(
                        result.getResponse()
                                .getContentAsString()
                );


        assertThat(response.get("id").asText())
                .isEqualTo(
                        incident.getId()
                );

        assertThat(response.get("title").asText())
                .isEqualTo(
                        "Database connection failures"
                );

        assertThat(response.get("status").asText())
                .isEqualTo("OPEN");
    }


    // =========================================================================
    // UNKNOWN INCIDENT
    // =========================================================================

    @Test
    void shouldReturnNotFoundForUnknownIncident()
            throws Exception {

        String token =
                loginAndGetToken(
                        adminUsername,
                        adminPassword
                );


        mockMvc.perform(
                        get(
                                "/api/incidents/{incidentId}",
                                "unknown-" + UUID.randomUUID()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNotFound());
    }


    // =========================================================================
    // QUERY INCIDENTS
    // =========================================================================

    @Test
    void shouldQueryIncidents() throws Exception {

        String token =
                loginAndGetToken(
                        adminUsername,
                        adminPassword
                );


        createIncident(
                "Payment failure",
                IncidentSeverity.CRITICAL
        );

        createIncident(
                "Payment latency",
                IncidentSeverity.HIGH
        );


        MvcResult result =
                mockMvc.perform(
                                get("/api/incidents")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .param("page", "0")
                                        .param("size", "20")
                        )
                        .andExpect(status().isOk())
                        .andReturn();


        JsonNode response =
                objectMapper.readTree(
                        result.getResponse()
                                .getContentAsString()
                );


        assertThat(response.get("content"))
                .isNotNull();

        assertThat(response.get("content").size())
                .isGreaterThanOrEqualTo(2);
    }


    // =========================================================================
    // QUERY BY STATUS
    // =========================================================================

    @Test
    void shouldQueryIncidentsByStatus()
            throws Exception {

        String token =
                loginAndGetToken(
                        adminUsername,
                        adminPassword
                );


        createIncident(
                "Open payment incident",
                IncidentSeverity.CRITICAL
        );


        MvcResult result =
                mockMvc.perform(
                                get("/api/incidents")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .param(
                                                "status",
                                                "OPEN"
                                        )
                                        .param("page", "0")
                                        .param("size", "20")
                        )
                        .andExpect(status().isOk())
                        .andReturn();


        JsonNode response =
                objectMapper.readTree(
                        result.getResponse()
                                .getContentAsString()
                );


        assertThat(response.get("content"))
                .isNotNull();


        for (JsonNode incident :
                response.get("content")) {

            assertThat(
                    incident.get("status").asText()
            ).isEqualTo("OPEN");
        }
    }


    // =========================================================================
    // QUERY BY SEVERITY
    // =========================================================================

    @Test
    void shouldQueryIncidentsBySeverity()
            throws Exception {

        String token =
                loginAndGetToken(
                        adminUsername,
                        adminPassword
                );


        createIncident(
                "Critical production failure",
                IncidentSeverity.CRITICAL
        );


        MvcResult result =
                mockMvc.perform(
                                get("/api/incidents")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .param(
                                                "severity",
                                                "CRITICAL"
                                        )
                                        .param("page", "0")
                                        .param("size", "20")
                        )
                        .andExpect(status().isOk())
                        .andReturn();


        JsonNode response =
                objectMapper.readTree(
                        result.getResponse()
                                .getContentAsString()
                );


        assertThat(response.get("content"))
                .isNotNull();


        for (JsonNode incident :
                response.get("content")) {

            assertThat(
                    incident.get("severity").asText()
            ).isEqualTo("CRITICAL");
        }
    }


    // =========================================================================
    // QUERY BY SERVICE
    // =========================================================================

    @Test
    void shouldQueryIncidentsByService()
            throws Exception {

        String token =
                loginAndGetToken(
                        adminUsername,
                        adminPassword
                );


        Incident incident =
                createIncident(
                        "Payment service failure",
                        IncidentSeverity.HIGH
                );


        MvcResult result =
                mockMvc.perform(
                                get("/api/incidents")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .param(
                                                "serviceName",
                                                incident.getServiceName()
                                        )
                                        .param("page", "0")
                                        .param("size", "20")
                        )
                        .andExpect(status().isOk())
                        .andReturn();


        JsonNode response =
                objectMapper.readTree(
                        result.getResponse()
                                .getContentAsString()
                );


        assertThat(response.get("content"))
                .isNotNull();

        assertThat(response.get("content").size())
                .isGreaterThanOrEqualTo(1);
    }


    // =========================================================================
    // QUERY BY ENVIRONMENT
    // =========================================================================

    @Test
    void shouldQueryIncidentsByEnvironment()
            throws Exception {

        String token =
                loginAndGetToken(
                        adminUsername,
                        adminPassword
                );


        createIncident(
                "Production incident",
                IncidentSeverity.HIGH
        );


        MvcResult result =
                mockMvc.perform(
                                get("/api/incidents")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .param(
                                                "environment",
                                                "production"
                                        )
                                        .param("page", "0")
                                        .param("size", "20")
                        )
                        .andExpect(status().isOk())
                        .andReturn();


        JsonNode response =
                objectMapper.readTree(
                        result.getResponse()
                                .getContentAsString()
                );


        assertThat(response.get("content"))
                .isNotNull();

        assertThat(response.get("content").size())
                .isGreaterThanOrEqualTo(1);
    }


    // =========================================================================
    // ACKNOWLEDGE
    // =========================================================================

    @Test
    void shouldAcknowledgeIncident()
            throws Exception {

        String token =
                loginAndGetToken(
                        adminUsername,
                        adminPassword
                );


        Incident incident =
                createIncident(
                        "Payment incident",
                        IncidentSeverity.HIGH
                );


        mockMvc.perform(
                        patch(
                                "/api/incidents/{incidentId}/acknowledge",
                                incident.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());


        Incident updated =
                incidentRepository
                        .findById(incident.getId())
                        .orElseThrow();


        assertThat(updated.getStatus())
                .isEqualTo(
                        IncidentStatus.ACKNOWLEDGED
                );
    }


    // =========================================================================
    // START INVESTIGATION
    // =========================================================================

    @Test
    void shouldStartInvestigation()
            throws Exception {

        String token =
                loginAndGetToken(
                        adminUsername,
                        adminPassword
                );


        Incident incident =
                createIncident(
                        "Database incident",
                        IncidentSeverity.HIGH
                );


        incident.acknowledge();

        incidentRepository.save(incident);


        mockMvc.perform(
                        patch(
                                "/api/incidents/{incidentId}/investigate",
                                incident.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());


        Incident updated =
                incidentRepository
                        .findById(incident.getId())
                        .orElseThrow();


        assertThat(updated.getStatus())
                .isEqualTo(
                        IncidentStatus.INVESTIGATING
                );

        assertThat(updated.getInvestigatingAt())
                .isNotNull();
    }


    // =========================================================================
    // MITIGATE
    // =========================================================================

    @Test
    void shouldMitigateIncident()
            throws Exception {

        String token =
                loginAndGetToken(
                        adminUsername,
                        adminPassword
                );


        Incident incident =
                createIncident(
                        "API failure",
                        IncidentSeverity.HIGH
                );


        incident.acknowledge();
        incident.startInvestigation();

        incidentRepository.save(incident);


        mockMvc.perform(
                        patch(
                                "/api/incidents/{incidentId}/mitigate",
                                incident.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());


        Incident updated =
                incidentRepository
                        .findById(incident.getId())
                        .orElseThrow();


        assertThat(updated.getStatus())
                .isEqualTo(
                        IncidentStatus.MITIGATED
                );

        assertThat(updated.getMitigatedAt())
                .isNotNull();
    }


    // =========================================================================
    // RESOLVE
    // =========================================================================

    @Test
    void shouldResolveIncident()
            throws Exception {

        String token =
                loginAndGetToken(
                        adminUsername,
                        adminPassword
                );


        Incident incident =
                createIncident(
                        "Resolved incident",
                        IncidentSeverity.MEDIUM
                );


        incident.acknowledge();
        incident.startInvestigation();
        incident.mitigate();

        incidentRepository.save(incident);


        mockMvc.perform(
                        patch(
                                "/api/incidents/{incidentId}/resolve",
                                incident.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());


        Incident updated =
                incidentRepository
                        .findById(incident.getId())
                        .orElseThrow();


        assertThat(updated.getStatus())
                .isEqualTo(
                        IncidentStatus.RESOLVED
                );

        assertThat(updated.getResolvedAt())
                .isNotNull();
    }


    // =========================================================================
    // CLOSE
    // =========================================================================

    @Test
    void shouldCloseIncident()
            throws Exception {

        String token =
                loginAndGetToken(
                        adminUsername,
                        adminPassword
                );


        Incident incident =
                createIncident(
                        "Closed incident",
                        IncidentSeverity.LOW
                );


        incident.acknowledge();
        incident.startInvestigation();
        incident.mitigate();
        incident.resolve();

        incidentRepository.save(incident);


        mockMvc.perform(
                        patch(
                                "/api/incidents/{incidentId}/close",
                                incident.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());


        Incident updated =
                incidentRepository
                        .findById(incident.getId())
                        .orElseThrow();


        assertThat(updated.getStatus())
                .isEqualTo(
                        IncidentStatus.CLOSED
                );

        assertThat(updated.getClosedAt())
                .isNotNull();
    }


    // =========================================================================
    // ASSIGN
    // =========================================================================

    @Test
    void shouldAssignIncident()
            throws Exception {

        String token =
                loginAndGetToken(
                        adminUsername,
                        adminPassword
                );


        Incident incident =
                createIncident(
                        "Incident requiring assignment",
                        IncidentSeverity.HIGH
                );


        String request = """
                {
                    "assignedTo": "engineer-123"
                }
                """;


        mockMvc.perform(
                        patch(
                                "/api/incidents/{incidentId}/assign",
                                incident.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(request)
                )
                .andExpect(status().isOk());


        Incident updated =
                incidentRepository
                        .findById(incident.getId())
                        .orElseThrow();


        assertThat(updated.getAssignedTo())
                .isEqualTo("engineer-123");
    }


    // =========================================================================
    // UNAUTHENTICATED
    // =========================================================================

    @Test
    void shouldRejectUnauthenticatedRequest()
            throws Exception {

        mockMvc.perform(
                        get("/api/incidents")
                                .param("page", "0")
                                .param("size", "20")
                )
                .andExpect(status().isUnauthorized());
    }


    @Test
    void shouldRejectUnauthenticatedIncidentCreation()
            throws Exception {

        String incidentNumber =
                "INC-" + UUID.randomUUID();


        String request = """
                {
                    "incidentNumber": "%s",
                    "title": "Unauthorized incident",
                    "description": "This request should be rejected",
                    "severity": "HIGH",
                    "serviceName": "payment-service",
                    "environment": "production"
                }
                """.formatted(incidentNumber);


        mockMvc.perform(
                        post("/api/incidents")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(request)
                )
                .andExpect(status().isUnauthorized());
    }


    // =========================================================================
    // VALIDATION
    // =========================================================================

    @Test
    void shouldRejectInvalidIncidentRequest()
            throws Exception {

        String token =
                loginAndGetToken(
                        adminUsername,
                        adminPassword
                );


        String invalidRequest = """
                {
                    "incidentNumber": "",
                    "title": "",
                    "description": "",
                    "severity": null,
                    "serviceName": "",
                    "environment": ""
                }
                """;


        MvcResult result =
                mockMvc.perform(
                                post("/api/incidents")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(invalidRequest)
                        )
                        .andExpect(status().isBadRequest())
                        .andReturn();


        assertThat(
                result.getResponse()
                        .getContentAsString()
        ).isNotBlank();
    }


    // =========================================================================
    // DUPLICATE INCIDENT
    // =========================================================================

    @Test
    void shouldRejectDuplicateIncidentNumber()
            throws Exception {

        String token =
                loginAndGetToken(
                        adminUsername,
                        adminPassword
                );


        String incidentNumber =
                "INC-" + UUID.randomUUID();


        String request = """
                {
                    "incidentNumber": "%s",
                    "title": "Duplicate test incident",
                    "description": "Duplicate incident test",
                    "severity": "HIGH",
                    "serviceName": "payment-service",
                    "environment": "production"
                }
                """.formatted(incidentNumber);


        mockMvc.perform(
                        post("/api/incidents")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(request)
                )
                .andExpect(status().isCreated());


        mockMvc.perform(
                        post("/api/incidents")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(request)
                )
                .andExpect(status().isConflict());
    }


    // =========================================================================
    // DELETE
    // =========================================================================

    @Test
    void shouldDeleteIncidentIfDeleteEndpointExists()
            throws Exception {

        String token =
                loginAndGetToken(
                        adminUsername,
                        adminPassword
                );


        Incident incident =
                createIncident(
                        "Incident to delete",
                        IncidentSeverity.LOW
                );


        MvcResult result =
                mockMvc.perform(
                                delete(
                                        "/api/incidents/{incidentId}",
                                        incident.getId()
                                )
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                        )
                        .andReturn();


        int responseStatus =
                result.getResponse().getStatus();


        assertThat(responseStatus)
                .isIn(
                        204,
                        404,
                        405
                );
    }


    // =========================================================================
    // TEST HELPERS
    // =========================================================================

    private Incident createIncident(
            String title,
            IncidentSeverity severity) {

        Incident incident =
                new Incident();


        incident.setIncidentNumber(
                "INC-" + UUID.randomUUID()
        );

        incident.setTitle(title);

        incident.setDescription(
                "Integration test incident"
        );

        incident.setSeverity(severity);

        /*
         * Explicitly set these values so this helper does not
         * depend on constructor defaults.
         */
        incident.setStatus(
                IncidentStatus.OPEN
        );

        incident.setServiceName(
                "payment-service"
        );

        incident.setEnvironment(
                "production"
        );

        incident.setDetectedAt(
                Instant.now()
        );


        return incidentRepository.save(incident);
    }


    private String loginAndGetToken(
            String username,
            String password) throws Exception {


        String loginRequest = """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(
                username,
                password
        );


        MvcResult loginResult =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(loginRequest)
                        )
                        .andExpect(status().isOk())
                        .andReturn();


        String loginResponse =
                loginResult
                        .getResponse()
                        .getContentAsString();


        assertThat(loginResponse)
                .as(
                        "Login API must return a response body"
                )
                .isNotBlank();


        JsonNode responseJson =
                objectMapper.readTree(
                        loginResponse
                );


        assertThat(responseJson)
                .as(
                        "Login response must be valid JSON"
                )
                .isNotNull();


        JsonNode tokenNode =
                responseJson.get("token");


        assertThat(tokenNode)
                .as(
                        "Login response must contain 'token'. Response: %s",
                        loginResponse
                )
                .isNotNull();


        assertThat(tokenNode.asText())
                .as(
                        "JWT token must not be empty"
                )
                .isNotBlank();


        return tokenNode.asText();
    }
}