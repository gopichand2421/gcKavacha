package com.gckavach.gckavachapp.incident.service;

import com.gckavach.gckavachapp.incident.api.IncidentSearchCriteria;
import com.gckavach.gckavachapp.incident.domain.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service responsible for searching incidents using dynamic criteria.
 *
 * <p>
 * The service supports multiple optional filters without requiring
 * a separate repository method for every possible filter combination.
 * </p>
 */
public interface IncidentSearchService {

    /**
     * Searches incidents using the supplied criteria.
     *
     * @param criteria optional incident search filters
     * @param pageable pagination and sorting information
     * @return paginated incidents
     */
    Page<Incident> search(
            IncidentSearchCriteria criteria,
            Pageable pageable
    );
}