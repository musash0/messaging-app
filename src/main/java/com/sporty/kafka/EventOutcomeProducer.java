package com.sporty.kafka;

import com.sporty.domain.EventOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes {@link EventOutcome} messages to the Kafka {@code event-outcomes} topic.
 *
 * <p>Called by {@link com.sporty.api.EventOutcomeController} from the REST layer. The topic
 * name is injected from configuration so tests and alternative environments can override it.
 *
 * <p>Each message is keyed by the string form of {@code eventId}. This guarantees that all
 * outcomes for the same event land on the same partition, preserving per-event ordering if
 * the topic is ever scaled to multiple partitions.
 */
@Component
public class EventOutcomeProducer {

    private static final Logger log = LoggerFactory.getLogger(EventOutcomeProducer.class);

    private final KafkaTemplate<String, EventOutcome> kafkaTemplate;
    private final String topic;

    public EventOutcomeProducer(KafkaTemplate<String, EventOutcome> kafkaTemplate,
                                @Value("${app.topics.event-outcomes}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    /**
     * Sends an event outcome to Kafka asynchronously.
     *
     * <p>The call is non-blocking — delivery is handled by the underlying
     * {@link KafkaTemplate} producer thread. Errors are logged by spring-kafka's default
     * producer listener; this method does not wait for the broker acknowledgement.
     *
     * @param outcome the outcome to publish; its {@code eventId} is used as the partition key
     */
    public void publish(EventOutcome outcome) {
        log.info("Publishing event outcome to Kafka topic '{}': {}", topic, outcome);
        kafkaTemplate.send(topic, String.valueOf(outcome.eventId()), outcome);
    }
}