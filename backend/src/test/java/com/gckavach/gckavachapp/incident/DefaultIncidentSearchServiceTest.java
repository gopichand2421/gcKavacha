package com.gckavach.gckavachapp.incident;

import com.gckavach.gckavachapp.incident.api.IncidentSearchCriteria;
import com.gckavach.gckavachapp.incident.domain.Incident;
import com.gckavach.gckavachapp.incident.domain.IncidentSeverity;
import com.gckavach.gckavachapp.incident.domain.IncidentStatus;
import com.gckavach.gckavachapp.incident.service.DefaultIncidentSearchService;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Unit tests for DefaultIncidentSearchService.
 *
 * <p>
 * MongoTemplate is mocked, so these tests do not require a running
 * MongoDB instance.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class DefaultIncidentSearchServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    private DefaultIncidentSearchService service;

    @BeforeEach
    void setUp() {
        service = new DefaultIncidentSearchService(mongoTemplate);
    }

    /**
     * Verifies that incidents are returned when no filters are supplied.
     */
    @Test
    void shouldReturnIncidentsWhenNoFiltersProvided() {

        Pageable pageable = PageRequest.of(0, 20);

        Incident incident = mockIncident("INC-1001");

        when(mongoTemplate.find(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(List.of(incident));

        when(mongoTemplate.count(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(1L);

        IncidentSearchCriteria criteria =
                new IncidentSearchCriteria(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        Page<Incident> result =
                service.search(criteria, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(
                "INC-1001",
                result.getContent().get(0).getIncidentNumber()
        );

        verify(mongoTemplate).find(
                any(Query.class),
                eq(Incident.class)
        );

        verify(mongoTemplate).count(
                any(Query.class),
                eq(Incident.class)
        );
    }

    /**
     * Verifies filtering by incident status.
     */
    @Test
    void shouldFilterByStatus() {

        Pageable pageable = PageRequest.of(0, 20);

        when(mongoTemplate.find(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(List.of());

        when(mongoTemplate.count(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(0L);

        IncidentSearchCriteria criteria =
                new IncidentSearchCriteria(
                        IncidentStatus.INVESTIGATING,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        service.search(criteria, pageable);

        Query query = captureFindQuery();

        assertCriteriaValue(
                query,
                "status",
                IncidentStatus.INVESTIGATING
        );
    }

    /**
     * Verifies filtering by incident severity.
     */
    @Test
    void shouldFilterBySeverity() {

        Pageable pageable = PageRequest.of(0, 20);

        when(mongoTemplate.find(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(List.of());

        when(mongoTemplate.count(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(0L);

        IncidentSearchCriteria criteria =
                new IncidentSearchCriteria(
                        null,
                        IncidentSeverity.CRITICAL,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        service.search(criteria, pageable);

        Query query = captureFindQuery();

        assertCriteriaValue(
                query,
                "severity",
                IncidentSeverity.CRITICAL
        );
    }

    /**
     * Verifies filtering by service name.
     */
    @Test
    void shouldFilterByServiceName() {

        Pageable pageable = PageRequest.of(0, 20);

        when(mongoTemplate.find(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(List.of());

        when(mongoTemplate.count(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(0L);

        IncidentSearchCriteria criteria =
                new IncidentSearchCriteria(
                        null,
                        null,
                        "payment-service",
                        null,
                        null,
                        null,
                        null
                );

        service.search(criteria, pageable);

        Query query = captureFindQuery();

        assertCriteriaValue(
                query,
                "serviceName",
                "payment-service"
        );
    }

    /**
     * Verifies filtering by environment.
     */
    @Test
    void shouldFilterByEnvironment() {

        Pageable pageable = PageRequest.of(0, 20);

        when(mongoTemplate.find(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(List.of());

        when(mongoTemplate.count(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(0L);

        IncidentSearchCriteria criteria =
                new IncidentSearchCriteria(
                        null,
                        null,
                        null,
                        "production",
                        null,
                        null,
                        null
                );

        service.search(criteria, pageable);

        Query query = captureFindQuery();

        assertCriteriaValue(
                query,
                "environment",
                "production"
        );
    }

    /**
     * Verifies partial incident-number search.
     *
     * <p>
     * The implementation uses a case-insensitive regular expression.
     * </p>
     */
    @Test
    void shouldFilterByIncidentNumber() {

        Pageable pageable = PageRequest.of(0, 20);

        when(mongoTemplate.find(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(List.of());

        when(mongoTemplate.count(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(0L);

        IncidentSearchCriteria criteria =
                new IncidentSearchCriteria(
                        null,
                        null,
                        null,
                        null,
                        "INC-10",
                        null,
                        null
                );

        service.search(criteria, pageable);

        Query query = captureFindQuery();

        assertRegexCriteriaExists(
                query,
                "incidentNumber",
                "INC-10"
        );
    }

    /**
     * Verifies partial title search.
     */
    @Test
    void shouldFilterByTitle() {

        Pageable pageable = PageRequest.of(0, 20);

        when(mongoTemplate.find(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(List.of());

        when(mongoTemplate.count(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(0L);

        IncidentSearchCriteria criteria =
                new IncidentSearchCriteria(
                        null,
                        null,
                        null,
                        null,
                        null,
                        "Payment failure",
                        null
                );

        service.search(criteria, pageable);

        Query query = captureFindQuery();

        assertRegexCriteriaExists(
                query,
                "title",
                "Payment failure"
        );
    }

    /**
     * Verifies filtering by assigned user.
     */
    @Test
    void shouldFilterByAssignedTo() {

        Pageable pageable = PageRequest.of(0, 20);

        when(mongoTemplate.find(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(List.of());

        when(mongoTemplate.count(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(0L);

        IncidentSearchCriteria criteria =
                new IncidentSearchCriteria(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "gopi"
                );

        service.search(criteria, pageable);

        Query query = captureFindQuery();

        assertCriteriaValue(
                query,
                "assignedTo",
                "gopi"
        );
    }

    /**
     * Verifies that multiple filters are combined using AND semantics.
     */
    @Test
    void shouldApplyMultipleFiltersTogether() {

        Pageable pageable = PageRequest.of(0, 20);

        when(mongoTemplate.find(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(List.of());

        when(mongoTemplate.count(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(0L);

        IncidentSearchCriteria criteria =
                new IncidentSearchCriteria(
                        IncidentStatus.INVESTIGATING,
                        IncidentSeverity.CRITICAL,
                        "payment-service",
                        "production",
                        null,
                        null,
                        "gopi"
                );

        service.search(criteria, pageable);

        Query query = captureFindQuery();

        assertCriteriaValue(
                query,
                "status",
                IncidentStatus.INVESTIGATING
        );

        assertCriteriaValue(
                query,
                "severity",
                IncidentSeverity.CRITICAL
        );

        assertCriteriaValue(
                query,
                "serviceName",
                "payment-service"
        );

        assertCriteriaValue(
                query,
                "environment",
                "production"
        );

        assertCriteriaValue(
                query,
                "assignedTo",
                "gopi"
        );
    }

    /**
     * Verifies pagination information.
     */
    @Test
    void shouldApplyPagination() {

        Pageable pageable =
                PageRequest.of(2, 10);

        when(mongoTemplate.find(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(List.of());

        when(mongoTemplate.count(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(25L);

        IncidentSearchCriteria criteria =
                emptyCriteria();

        Page<Incident> result =
                service.search(criteria, pageable);

        assertEquals(25L, result.getTotalElements());
        assertEquals(2, result.getNumber());
        assertEquals(10, result.getSize());
    }

    /**
     * Verifies descending sorting.
     *
     * <p>
     * MongoDB represents descending sort direction as -1.
     * Ascending is represented as 1.
     * </p>
     */
    @Test
    void shouldApplySorting() {

        Pageable pageable =
                PageRequest.of(
                        0,
                        20,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        when(mongoTemplate.find(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(List.of());

        when(mongoTemplate.count(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(0L);

        service.search(
                emptyCriteria(),
                pageable
        );

        Query query = captureFindQuery();

        Document sortDocument =
                query.getSortObject();

        assertNotNull(sortDocument);

        assertEquals(
                -1,
                sortDocument.getInteger("createdAt")
        );
    }

    /**
     * Verifies that blank string filters are ignored.
     */
    @Test
    void shouldIgnoreBlankStringFilters() {

        Pageable pageable = PageRequest.of(0, 20);

        when(mongoTemplate.find(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(List.of());

        when(mongoTemplate.count(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(0L);

        IncidentSearchCriteria criteria =
                new IncidentSearchCriteria(
                        null,
                        null,
                        "   ",
                        " ",
                        "",
                        "   ",
                        " "
                );

        service.search(criteria, pageable);

        Query query = captureFindQuery();

        Document queryDocument =
                query.getQueryObject();

        /*
         * No filter should be present.
         */
        assertFalse(
                queryDocument.containsKey("$and")
        );

        assertEquals(
                0,
                queryDocument.size()
        );
    }

    /**
     * Verifies that filters containing leading/trailing spaces
     * are trimmed before being added to the query.
     */
    @Test
    void shouldTrimStringFilters() {

        Pageable pageable = PageRequest.of(0, 20);

        when(mongoTemplate.find(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(List.of());

        when(mongoTemplate.count(
                any(Query.class),
                eq(Incident.class)
        )).thenReturn(0L);

        IncidentSearchCriteria criteria =
                new IncidentSearchCriteria(
                        null,
                        null,
                        "  payment-service  ",
                        "  production  ",
                        null,
                        null,
                        "  gopi  "
                );

        service.search(criteria, pageable);

        Query query = captureFindQuery();

        assertCriteriaValue(
                query,
                "serviceName",
                "payment-service"
        );

        assertCriteriaValue(
                query,
                "environment",
                "production"
        );

        assertCriteriaValue(
                query,
                "assignedTo",
                "gopi"
        );
    }

    /**
     * Creates empty search criteria.
     */
    private IncidentSearchCriteria emptyCriteria() {

        return new IncidentSearchCriteria(
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    /**
     * Captures the Query passed to MongoTemplate.find().
     */
    private Query captureFindQuery() {

        ArgumentCaptor<Query> captor =
                ArgumentCaptor.forClass(Query.class);

        verify(mongoTemplate).find(
                captor.capture(),
                eq(Incident.class)
        );

        return captor.getValue();
    }

    /**
     * Finds a normal equality criterion inside the generated Mongo query.
     *
     * <p>
     * DefaultIncidentSearchService uses:
     *
     * new Criteria().andOperator(...)
     *
     * Therefore the generated query has the following general structure:
     *
     * {
     *     "$and": [
     *         {
     *             "status": "INVESTIGATING"
     *         }
     *     ]
     * }
     * </p>
     */
    private void assertCriteriaValue(
            Query query,
            String field,
            Object expectedValue
    ) {

        Document queryDocument =
                query.getQueryObject();

        Object andObject =
                queryDocument.get("$and");

        assertNotNull(
                andObject,
                "Expected $and criteria for field: " + field
        );

        List<?> criteriaList =
                (List<?>) andObject;

        boolean found = false;

        for (Object criterion : criteriaList) {

            if (!(criterion instanceof Document document)) {
                continue;
            }

            if (!document.containsKey(field)) {
                continue;
            }

            Object actualValue =
                    document.get(field);

            assertEquals(
                    expectedValue,
                    actualValue,
                    "Unexpected value for field: " + field
            );

            found = true;
            break;
        }

        assertTrue(
                found,
                "Expected field '" + field +
                        "' was not present in query: " +
                        queryDocument
        );
    }

    /**
     * Verifies a regex-based search criterion.
     *
     * <p>
     * DefaultIncidentSearchService uses:
     *
     * Pattern.quote(value)
     *
     * when constructing the regex.
     *
     * <p>
     * Therefore Spring Data MongoDB represents the criterion as:
     *
     * {
     *     "$and": [
     *         {
     *             "title": "\\QPayment failure\\E"
     *         }
     *     ]
     * }
     *
     * rather than:
     *
     * {
     *     "$and": [
     *         {
     *             "title": {
     *                 "$regex": "Payment failure"
     *             }
     *         }
     *     ]
     * }
     */
    private void assertRegexCriteriaExists(
            Query query,
            String field,
            String expectedText
    ) {

        Document queryDocument =
                query.getQueryObject();

        Object andObject =
                queryDocument.get("$and");

        assertNotNull(
                andObject,
                "Expected $and criteria for field: " + field
        );

        List<?> criteriaList =
                (List<?>) andObject;

        boolean found = false;

        for (Object criterion : criteriaList) {

            if (!(criterion instanceof Document document)) {
                continue;
            }

            /*
             * Retrieve the value associated with the field.
             */
            Object fieldValue =
                    document.get(field);

            if (fieldValue == null) {
                continue;
            }

            /*
             * Pattern.quote("Payment failure") produces:
             *
             * \QPayment failure\E
             */
            String actualValue =
                    fieldValue.toString();

            String expectedRegex =
                    java.util.regex.Pattern.quote(expectedText);

            if (actualValue.equals(expectedRegex)) {
                found = true;
                break;
            }
        }

        assertTrue(
                found,
                "Expected regex criterion for field '" +
                        field +
                        "' was not present in query: " +
                        queryDocument
        );
    }

    /**
     * Creates a minimal mocked Incident.
     */
    private Incident mockIncident(String incidentNumber) {

        Incident incident =
                org.mockito.Mockito.mock(Incident.class);

        when(incident.getIncidentNumber())
                .thenReturn(incidentNumber);

        return incident;
    }
}