package com.gckavach.gckavachapp.incident;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gckavach.gckavachapp.incident.domain.Incident;
import com.gckavach.gckavachapp.incident.domain.IncidentSeverity;
import com.gckavach.gckavachapp.incident.domain.IncidentStatus;
import com.gckavach.gckavachapp.incident.repository.IncidentRepository;
import com.gckavach.gckavachapp.user.domain.Role;
import com.gckavach.gckavachapp.user.domain.User;
import com.gckavach.gckavachapp.user.domain.UserStatus;
import com.gckavach.gckavachapp.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the incident search API.
 *
 * <p>
 * These tests verify the complete flow:
 *
 * <pre>
 * HTTP request
 *      |
 * IncidentController
 *      |
 * IncidentService
 *      |
 * IncidentSearchService
 *      |
 * MongoDB
 * </pre>
 *
 * <p>
 * The tests cover:
 * <ul>
 *     <li>Authentication</li>
 *     <li>Search without filters</li>
 *     <li>Status filtering</li>
 *     <li>Severity filtering</li>
 *     <li>Service filtering</li>
 *     <li>Environment filtering</li>
 *     <li>Incident number search</li>
 *     <li>Title search</li>
 *     <li>Assigned user filtering</li>
 *     <li>Multiple filters</li>
 *     <li>Pagination</li>
 *     <li>Sorting</li>
 *     <li>Empty results</li>
 * </ul>
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "JWT_SECRET=gckavacha-test-jwt-secret-key-2026-minimum-32-bytes",
        "JWT_EXPIRATION=3600"
})
class IncidentSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Test-local ObjectMapper.
     *
     * <p>
     * The application context does not expose an ObjectMapper bean,
     * so this test creates one explicitly.
     */
    private final ObjectMapper objectMapper =
            new ObjectMapper().findAndRegisterModules();

    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {

        /*
         * Clean incidents before every test.
         */
        incidentRepository.deleteAll();

        /*
         * Clean users before every test.
         */
        userRepository.deleteAll();

        /*
         * Create test user.
         */
        User user = new User();

        user.setUsername("search-test-user");
        user.setEmail("search-test@gckavacha.com");
        user.setPasswordHash(
                passwordEncoder.encode("Password@123")
        );
        user.setFirstName("Search");
        user.setLastName("Tester");
        user.setRoles(Set.of(Role.USER));
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        /*
         * Login and obtain JWT.
         */
        accessToken = loginAndGetToken();
    }

    /**
     * Verifies that the search endpoint requires authentication.
     */
    @Test
    void shouldReturn401WhenSearchingWithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                        get("/api/incidents/search")
                )
                .andExpect(status().isUnauthorized());
    }

    /**
     * Verifies that searching without filters returns all incidents.
     */
    @Test
    void shouldReturnIncidentsWhenSearchingWithoutFilters()
            throws Exception {

        saveIncident(
                "INC-1001",
                "Payment failure",
                IncidentSeverity.CRITICAL,
                IncidentStatus.OPEN,
                "payment-service",
                "production",
                "gopi"
        );

        saveIncident(
                "INC-1002",
                "Order failure",
                IncidentSeverity.HIGH,
                IncidentStatus.INVESTIGATING,
                "order-service",
                "production",
                "ravi"
        );

        mockMvc.perform(
                        get("/api/incidents/search")
                                .header(
                                        "Authorization",
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    /**
     * Verifies status filtering.
     */
    @Test
    void shouldFilterByStatus() throws Exception {

        saveIncident(
                "INC-1001",
                "Payment failure",
                IncidentSeverity.CRITICAL,
                IncidentStatus.OPEN,
                "payment-service",
                "production",
                "gopi"
        );

        saveIncident(
                "INC-1002",
                "Order failure",
                IncidentSeverity.HIGH,
                IncidentStatus.INVESTIGATING,
                "order-service",
                "production",
                "ravi"
        );

        mockMvc.perform(
                        get("/api/incidents/search")
                                .param(
                                        "status",
                                        "INVESTIGATING"
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(
                        jsonPath(
                                "$.content[0].incidentNumber",
                                is("INC-1002")
                        )
                );
    }

    /**
     * Verifies severity filtering.
     */
    @Test
    void shouldFilterBySeverity() throws Exception {

        saveIncident(
                "INC-1001",
                "Payment failure",
                IncidentSeverity.CRITICAL,
                IncidentStatus.OPEN,
                "payment-service",
                "production",
                "gopi"
        );

        saveIncident(
                "INC-1002",
                "Order failure",
                IncidentSeverity.HIGH,
                IncidentStatus.OPEN,
                "order-service",
                "production",
                "ravi"
        );

        mockMvc.perform(
                        get("/api/incidents/search")
                                .param(
                                        "severity",
                                        "CRITICAL"
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(
                        jsonPath(
                                "$.content[0].severity",
                                is("CRITICAL")
                        )
                );
    }

    /**
     * Verifies service-name filtering.
     */
    @Test
    void shouldFilterByServiceName() throws Exception {

        saveIncident(
                "INC-1001",
                "Payment failure",
                IncidentSeverity.CRITICAL,
                IncidentStatus.OPEN,
                "payment-service",
                "production",
                "gopi"
        );

        saveIncident(
                "INC-1002",
                "Order failure",
                IncidentSeverity.HIGH,
                IncidentStatus.OPEN,
                "order-service",
                "production",
                "ravi"
        );

        mockMvc.perform(
                        get("/api/incidents/search")
                                .param(
                                        "serviceName",
                                        "payment-service"
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(
                        jsonPath(
                                "$.content[0].serviceName",
                                is("payment-service")
                        )
                );
    }

    /**
     * Verifies environment filtering.
     */
    @Test
    void shouldFilterByEnvironment() throws Exception {

        saveIncident(
                "INC-1001",
                "Production payment failure",
                IncidentSeverity.CRITICAL,
                IncidentStatus.OPEN,
                "payment-service",
                "production",
                "gopi"
        );

        saveIncident(
                "INC-1002",
                "Development payment failure",
                IncidentSeverity.HIGH,
                IncidentStatus.OPEN,
                "payment-service",
                "development",
                "gopi"
        );

        mockMvc.perform(
                        get("/api/incidents/search")
                                .param(
                                        "environment",
                                        "production"
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(
                        jsonPath(
                                "$.content[0].environment",
                                is("production")
                        )
                );
    }

    /**
     * Verifies partial incident-number search.
     */
    @Test
    void shouldSearchByIncidentNumber() throws Exception {

        saveIncident(
                "INC-1001",
                "Payment failure",
                IncidentSeverity.CRITICAL,
                IncidentStatus.OPEN,
                "payment-service",
                "production",
                "gopi"
        );

        saveIncident(
                "INC-2001",
                "Order failure",
                IncidentSeverity.HIGH,
                IncidentStatus.OPEN,
                "order-service",
                "production",
                "ravi"
        );

        mockMvc.perform(
                        get("/api/incidents/search")
                                .param(
                                        "incidentNumber",
                                        "1001"
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(
                        jsonPath(
                                "$.content[0].incidentNumber",
                                is("INC-1001")
                        )
                );
    }

    /**
     * Verifies case-insensitive title search.
     */
    @Test
    void shouldSearchByTitle() throws Exception {

        saveIncident(
                "INC-1001",
                "Payment Gateway Failure",
                IncidentSeverity.CRITICAL,
                IncidentStatus.OPEN,
                "payment-service",
                "production",
                "gopi"
        );

        mockMvc.perform(
                        get("/api/incidents/search")
                                .param(
                                        "title",
                                        "payment gateway"
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(
                        jsonPath(
                                "$.content[0].incidentNumber",
                                is("INC-1001")
                        )
                );
    }

    /**
     * Verifies assigned-user filtering.
     */
    @Test
    void shouldFilterByAssignedTo() throws Exception {

        saveIncident(
                "INC-1001",
                "Payment failure",
                IncidentSeverity.CRITICAL,
                IncidentStatus.OPEN,
                "payment-service",
                "production",
                "gopi"
        );

        saveIncident(
                "INC-1002",
                "Order failure",
                IncidentSeverity.HIGH,
                IncidentStatus.OPEN,
                "order-service",
                "production",
                "ravi"
        );

        mockMvc.perform(
                        get("/api/incidents/search")
                                .param(
                                        "assignedTo",
                                        "gopi"
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(
                        jsonPath(
                                "$.content[0].assignedTo",
                                is("gopi")
                        )
                );
    }

    /**
     * Verifies multiple filters use AND semantics.
     */
    @Test
    void shouldApplyMultipleFilters() throws Exception {

        saveIncident(
                "INC-1001",
                "Payment failure",
                IncidentSeverity.CRITICAL,
                IncidentStatus.INVESTIGATING,
                "payment-service",
                "production",
                "gopi"
        );

        saveIncident(
                "INC-1002",
                "Payment failure",
                IncidentSeverity.HIGH,
                IncidentStatus.INVESTIGATING,
                "payment-service",
                "production",
                "gopi"
        );

        saveIncident(
                "INC-1003",
                "Payment failure",
                IncidentSeverity.CRITICAL,
                IncidentStatus.OPEN,
                "payment-service",
                "production",
                "gopi"
        );

        mockMvc.perform(
                        get("/api/incidents/search")
                                .param(
                                        "status",
                                        "INVESTIGATING"
                                )
                                .param(
                                        "severity",
                                        "CRITICAL"
                                )
                                .param(
                                        "serviceName",
                                        "payment-service"
                                )
                                .param(
                                        "environment",
                                        "production"
                                )
                                .param(
                                        "assignedTo",
                                        "gopi"
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(
                        jsonPath(
                                "$.content[0].incidentNumber",
                                is("INC-1001")
                        )
                );
    }

    /**
     * Verifies pagination.
     */
    @Test
    void shouldApplyPagination() throws Exception {

        for (int i = 1; i <= 5; i++) {

            saveIncident(
                    "INC-10" + i,
                    "Payment failure " + i,
                    IncidentSeverity.HIGH,
                    IncidentStatus.OPEN,
                    "payment-service",
                    "production",
                    "gopi"
            );
        }

        mockMvc.perform(
                        get("/api/incidents/search")
                                .param("page", "0")
                                .param("size", "2")
                                .header(
                                        "Authorization",
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(
                        jsonPath(
                                "$.totalElements",
                                is(5)
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.size",
                                is(2)
                        )
                )
                .andExpect(
                        jsonPath(
                                "$.number",
                                is(0)
                        )
                );
    }

    /**
     * Verifies descending sorting.
     */
    @Test
    void shouldSortByCreatedAtDescending() throws Exception {

        saveIncident(
                "INC-1001",
                "First incident",
                IncidentSeverity.HIGH,
                IncidentStatus.OPEN,
                "payment-service",
                "production",
                "gopi"
        );

        Thread.sleep(20);

        saveIncident(
                "INC-1002",
                "Second incident",
                IncidentSeverity.HIGH,
                IncidentStatus.OPEN,
                "payment-service",
                "production",
                "gopi"
        );

        mockMvc.perform(
                        get("/api/incidents/search")
                                .param(
                                        "sort",
                                        "createdAt,desc"
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.content[0].incidentNumber",
                                is("INC-1002")
                        )
                );
    }

    /**
     * Verifies empty search results.
     */
    @Test
    void shouldReturnEmptyResultWhenNoIncidentMatches()
            throws Exception {

        saveIncident(
                "INC-1001",
                "Payment failure",
                IncidentSeverity.CRITICAL,
                IncidentStatus.OPEN,
                "payment-service",
                "production",
                "gopi"
        );

        mockMvc.perform(
                        get("/api/incidents/search")
                                .param(
                                        "serviceName",
                                        "unknown-service"
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(
                        jsonPath(
                                "$.totalElements",
                                is(0)
                        )
                );
    }

    /**
     * Saves an incident directly to MongoDB.
     *
     * <p>
     * Direct repository insertion keeps these tests focused on
     * search behavior instead of incident creation API behavior.
     * </p>
     */
    private Incident saveIncident(
            String incidentNumber,
            String title,
            IncidentSeverity severity,
            IncidentStatus status,
            String serviceName,
            String environment,
            String assignedTo
    ) {

        Incident incident = new Incident();

        incident.setIncidentNumber(incidentNumber);
        incident.setTitle(title);
        incident.setDescription("Integration test incident");
        incident.setSeverity(severity);
        incident.setStatus(status);
        incident.setServiceName(serviceName);
        incident.setEnvironment(environment);
        incident.setAssignedTo(assignedTo);

        return incidentRepository.save(incident);
    }

    /**
     * Logs in the test user and returns the JWT.
     */
    private String loginAndGetToken() throws Exception {

        String requestBody = """
                {
                  "username": "search-test-user",
                  "password": "Password@123"
                }
                """;

        String response =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(requestBody)
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        if (!json.hasNonNull("token")) {
            throw new IllegalStateException(
                    "Login response does not contain token: " + response
            );
        }

        return json.get("token").asText();
    }
}
