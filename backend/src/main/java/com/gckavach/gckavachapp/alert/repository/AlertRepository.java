package com.gckavach.gckavachapp.alert.repository;

import com.gckavach.gckavachapp.alert.domain.Alert;
import com.gckavach.gckavachapp.alert.domain.AlertSeverity;
import com.gckavach.gckavachapp.alert.domain.AlertStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AlertRepository extends MongoRepository<Alert, String> {
    Optional<Alert> findByExternalAlertId(String externalAlertId);

    Optional<Alert> findByFingerprint(String fingerprint);

    Page<Alert> findByStatus(AlertStatus status, Pageable pageable);

    Page<Alert> findBySeverity(AlertSeverity severity, Pageable pageable);

    Page<Alert> findByServiceName(String serviceName, Pageable pageable);

    Page<Alert> findByEnvironment(String environment, Pageable pageable);

    Page<Alert> findByStatusAndSeverity(AlertStatus status, AlertSeverity severity, Pageable pageable);

    Page<Alert> findByStatusAndServiceName(AlertStatus status, String serviceName, Pageable pageable);

    Page<Alert> findByStatusAndEnvironment(AlertStatus status, String environment, Pageable pageable);

    Page<Alert> findBySeverityAndServiceName(AlertSeverity severity, String serviceName, Pageable pageable);

    Page<Alert> findByServiceNameAndEnvironment(String serviceName, String environment, Pageable pageable);

    Page<Alert> findByStatusAndSeverityAndServiceName(AlertStatus status, AlertSeverity severity, String serviceName, Pageable pageable);

    Page<Alert> findByStatusAndSeverityAndServiceNameAndEnvironment(AlertStatus status, AlertSeverity severity, String serviceName, String environment, Pageable pageable);

    boolean existsByFingerprintAndStatus(String fingerprint, AlertStatus status);

    List<Alert> findByServiceNameAndEnvironmentAndStartedAtBetween(String serviceName, String environment, Instant from, Instant to);
}