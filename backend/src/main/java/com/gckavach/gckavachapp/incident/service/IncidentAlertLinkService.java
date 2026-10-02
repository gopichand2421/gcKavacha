package com.gckavach.gckavachapp.incident.service;

import com.gckavach.gckavachapp.incident.api.IncidentAlertLinkResponse;

import java.util.List;

public interface IncidentAlertLinkService {

    IncidentAlertLinkResponse linkAlertToIncident(
            String incidentId,
            String alertId
    );

    void unlinkAlertFromIncident(
            String incidentId,
            String alertId
    );

    List<IncidentAlertLinkResponse> getAlertsForIncident(
            String incidentId
    );

    List<IncidentAlertLinkResponse> getIncidentsForAlert(
            String alertId
    );
}