package com.sporty.kafka;

import com.sporty.domain.EventOutcome;
import com.sporty.service.SettlementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka listener for the {@code event-outcomes} topic.
 *
 * <p>Runs on a Spring-managed consumer in the {@code sporty-messaging} group. Each received
 * {@link EventOutcome} is handed straight to {@link SettlementService}, which owns the
 * domain logic (matching pending bets and triggering settlement messages). Keeping the
 * listener as a pure delegate makes the consumer easy to test and keeps messaging concerns
 * out of the service layer.
 *
 * <p>JSON deserialization is configured in {@code application.yml}; the default value type
 * is pinned to {@link EventOutcome} so that messages produced by arbitrary clients
 * deserialize correctly.
 */
@Component
public class EventOutcomeConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventOutcomeConsumer.class);

    private final SettlementService settlementService;

    public EventOutcomeConsumer(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    /**
     * Invoked by Spring Kafka for each message received on the {@code event-outcomes} topic.
     *
     * @param outcome the deserialized event outcome; guaranteed non-null when processed
     */
    @KafkaListener(topics = "${app.topics.event-outcomes}", groupId = "${spring.kafka.consumer.group-id}")
    public void onEventOutcome(EventOutcome outcome) {
        log.info("Received event outcome from Kafka: {}", outcome);
        settlementService.process(outcome);
    }
}