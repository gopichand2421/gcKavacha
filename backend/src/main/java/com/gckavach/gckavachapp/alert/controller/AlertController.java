package com.gckavach.gckavachapp.alert.controller;

import com.gckavach.gckavachapp.alert.api.AlertCreateRequest;
import com.gckavach.gckavachapp.alert.api.AlertPageResponse;
import com.gckavach.gckavachapp.alert.api.AlertQuery;
import com.gckavach.gckavachapp.alert.api.AlertResponse;
import com.gckavach.gckavachapp.alert.domain.Alert;
import com.gckavach.gckavachapp.alert.domain.AlertSeverity;
import com.gckavach.gckavachapp.alert.domain.AlertStatus;
import com.gckavach.gckavachapp.alert.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping
    public ResponseEntity<AlertResponse> createAlert(@Valid @RequestBody AlertCreateRequest request) {
        AlertResponse response = alertService.createAlert(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(alertService.createAlert(request));
    }

    @GetMapping
    public ResponseEntity<AlertPageResponse> getAlerts(@RequestParam(required = false) AlertStatus status, @RequestParam(required = false) AlertSeverity severity, @RequestParam(required = false) String serviceName, @RequestParam(required = false) String environment, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
        AlertQuery query = new AlertQuery(status, severity, serviceName, environment);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startedAt"));
        Page<AlertResponse> alerts =
                alertService.queryAlerts(
                        AlertStatus.OPEN,
                        AlertSeverity.CRITICAL,
                        "payment-service",
                        "production",
                        pageable
                );
        return ResponseEntity.ok(AlertPageResponse.from(alerts));
    }

    @PatchMapping("/{alertId}/acknowledge")
    public ResponseEntity<AlertResponse> acknowledgeAlert(@PathVariable String alertId) {
        return ResponseEntity.ok(alertService.acknowledgeAlert(alertId));
    }

    @PatchMapping("/{alertId}/resolve")
    public ResponseEntity<AlertResponse> resolveAlert(@PathVariable String alertId) {
        return ResponseEntity.ok(alertService.resolveAlert(alertId));
    }

    @PatchMapping("/{alertId}/suppress")
    public ResponseEntity<AlertResponse> suppressAlert(@PathVariable String alertId) {
        return ResponseEntity.ok(alertService.suppressAlert(alertId));
    }
}