package com.gckavach.gckavachapp.alert.repository;

import com.gckavach.gckavachapp.alert.correlation.AlertCorrelation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AlertCorrelationRepository
        extends MongoRepository<AlertCorrelation, String> {

    Optional<AlertCorrelation> findByPrimaryAlertIdAndRelatedAlertId(
            String primaryAlertId,
            String relatedAlertId
    );

    List<AlertCorrelation> findByPrimaryAlertId(
            String primaryAlertId
    );

    List<AlertCorrelation> findByRelatedAlertId(
            String relatedAlertId
    );

    List<AlertCorrelation> findByPrimaryAlertIdOrRelatedAlertId(
            String primaryAlertId,
            String relatedAlertId
    );

    boolean existsByPrimaryAlertIdAndRelatedAlertId(
            String primaryAlertId,
            String relatedAlertId
    );

    void deleteByPrimaryAlertId(String primaryAlertId);

    void deleteByRelatedAlertId(String relatedAlertId);
}