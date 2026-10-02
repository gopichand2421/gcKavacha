package com.gckavach.gckavachapp.incident.service;

import com.gckavach.gckavachapp.incident.api.IncidentSearchCriteria;
import com.gckavach.gckavachapp.incident.domain.Incident;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of incident search.
 *
 * <p>
 * Uses MongoTemplate so that search criteria can be built dynamically.
 * Only the filters provided by the caller are added to the MongoDB query.
 * </p>
 */
@Service
public class DefaultIncidentSearchService implements IncidentSearchService {

    private static final Logger log =
            LoggerFactory.getLogger(DefaultIncidentSearchService.class);

    private final MongoTemplate mongoTemplate;

    /**
     * Creates the incident search service.
     *
     * @param mongoTemplate MongoDB template used for dynamic queries
     */
    public DefaultIncidentSearchService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;

        log.debug("DefaultIncidentSearchService initialized");
    }

    /**
     * Searches incidents using dynamic filters.
     *
     * <p>
     * Multiple filters are combined using AND semantics.
     * </p>
     *
     * @param criteria search criteria
     * @param pageable pagination and sorting configuration
     * @return paginated incidents
     */
    @Override
    public Page<Incident> search(
            IncidentSearchCriteria criteria,
            Pageable pageable
    ) {

        log.debug(
                "Searching incidents: status={}, severity={}, serviceName={}, " +
                        "environment={}, incidentNumber={}, title={}, assignedTo={}, page={}, size={}",
                criteria.status(),
                criteria.severity(),
                criteria.serviceName(),
                criteria.environment(),
                criteria.incidentNumber(),
                criteria.title(),
                criteria.assignedTo(),
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        /*
         * Build the MongoDB query dynamically.
         */
        Query query = new Query();

        List<Criteria> criteriaList = new ArrayList<>();

        /*
         * Filter by incident status.
         */
        if (criteria.status() != null) {
            criteriaList.add(
                    Criteria.where("status")
                            .is(criteria.status())
            );
        }

        /*
         * Filter by incident severity.
         */
        if (criteria.severity() != null) {
            criteriaList.add(
                    Criteria.where("severity")
                            .is(criteria.severity())
            );
        }

        /*
         * Filter by service name.
         */
        if (hasText(criteria.serviceName())) {
            criteriaList.add(
                    Criteria.where("serviceName")
                            .is(criteria.serviceName().trim())
            );
        }

        /*
         * Filter by environment.
         */
        if (hasText(criteria.environment())) {
            criteriaList.add(
                    Criteria.where("environment")
                            .is(criteria.environment().trim())
            );
        }

        /*
         * Search incident number.
         *
         * Using regex makes the search partial and case-insensitive.
         *
         * Example:
         *
         * INC-10
         *
         * can match:
         *
         * INC-1001
         * INC-1002
         */
        if (hasText(criteria.incidentNumber())) {
            criteriaList.add(
                    Criteria.where("incidentNumber")
                            .regex(
                                    escapeRegex(criteria.incidentNumber().trim()),
                                    "i"
                            )
            );
        }

        /*
         * Search incident title.
         *
         * This is also a partial and case-insensitive search.
         */
        if (hasText(criteria.title())) {
            criteriaList.add(
                    Criteria.where("title")
                            .regex(
                                    escapeRegex(criteria.title().trim()),
                                    "i"
                            )
            );
        }

        /*
         * Filter incidents assigned to a particular user.
         */
        if (hasText(criteria.assignedTo())) {
            criteriaList.add(
                    Criteria.where("assignedTo")
                            .is(criteria.assignedTo().trim())
            );
        }

        /*
         * Add all criteria using AND semantics.
         */
        if (!criteriaList.isEmpty()) {
            query.addCriteria(
                    new Criteria().andOperator(
                            criteriaList.toArray(new Criteria[0])
                    )
            );
        }

        /*
         * Apply pagination and sorting.
         */
        query.with(pageable);

        /*
         * Fetch the current page.
         */
        List<Incident> incidents =
                mongoTemplate.find(query, Incident.class);

        /*
         * MongoTemplate does not automatically provide the total count
         * required by Spring's Page abstraction.
         *
         * Therefore, create a separate count query.
         */
        Query countQuery = Query.of(query);

        /*
         * Pagination must not be applied to the count query.
         */
        countQuery.skip(0);
        countQuery.limit(0);

        long total =
                mongoTemplate.count(countQuery, Incident.class);

        log.debug(
                "Incident search completed: results={}, total={}",
                incidents.size(),
                total
        );

        return new PageImpl<>(
                incidents,
                pageable,
                total
        );
    }

    /**
     * Checks whether a string contains meaningful text.
     */
    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Escapes user supplied text before putting it into a regex.
     *
     * <p>
     * This prevents characters such as '.', '*', '+', etc.
     * from changing the intended search expression.
     * </p>
     */
    private String escapeRegex(String value) {
        return java.util.regex.Pattern
                .quote(value);
    }
}