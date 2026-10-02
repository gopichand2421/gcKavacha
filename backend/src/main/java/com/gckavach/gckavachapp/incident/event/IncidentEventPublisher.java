package com.gckavach.gckavachapp.incident.event;

/**
 * Publishes incident domain events.
 *
 * <p>The incident service depends on this abstraction rather than directly
 * depending on Kafka. This keeps the business logic independent of the
 * messaging technology.</p>
 *
 * <p>The current implementation will use Kafka, but another implementation
 * can be introduced later without changing the incident service.</p>
 */
public interface IncidentEventPublisher {

    /**
     * Publishes an incident event.
     *
     * @param event incident event to publish
     */
    void publish(IncidentEvent event);
}