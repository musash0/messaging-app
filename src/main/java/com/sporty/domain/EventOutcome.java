package com.sporty.domain;

/**
 * Domain record describing the final outcome of a sports event.
 *
 * <p>Published to the {@code event-outcomes} Kafka topic by the REST layer and consumed by
 * {@link com.sporty.kafka.EventOutcomeConsumer}, which hands it to the settlement service.
 * All pending bets on {@code eventId} are evaluated against {@code eventWinnerId}.
 *
 * @param eventId       identifier of the sports event that has concluded
 * @param eventName     human-readable name of the event, preserved for logging/audit
 * @param eventWinnerId identifier of the winning side / competitor in this event
 */
public record EventOutcome(
        Long eventId,
        String eventName,
        Long eventWinnerId) {
}