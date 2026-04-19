package com.sporty.api.dto;

import com.sporty.domain.EventOutcome;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * HTTP request DTO for {@code POST /api/v1/event-outcomes}.
 *
 * <p>Kept separate from the domain {@link EventOutcome} record so that wire-format concerns
 * (JSON field names, bean-validation annotations, HTTP error mapping) do not leak into the
 * domain model passed through Kafka.
 *
 * @param eventId       identifier of the sports event that has concluded; required
 * @param eventName     human-readable name of the event (e.g. "Lakers vs Celtics"); required, non-blank
 * @param eventWinnerId identifier of the winning side / competitor in this event; required
 */
public record EventOutcomeRequest(
        @NotNull Long eventId,
        @NotBlank String eventName,
        @NotNull Long eventWinnerId
) {
    /**
     * Converts the validated request into the domain record that is published to Kafka.
     *
     * @return an {@link EventOutcome} with the same field values
     */
    public EventOutcome toDomain() {
        return new EventOutcome(eventId, eventName, eventWinnerId);
    }
}