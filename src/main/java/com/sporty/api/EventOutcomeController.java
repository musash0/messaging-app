package com.sporty.api;

import com.sporty.api.dto.EventOutcomeRequest;
import com.sporty.kafka.EventOutcomeProducer;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public HTTP entry point for publishing sports-event outcomes.
 *
 * <p>This controller is intentionally thin: it validates the incoming JSON payload, converts
 * the DTO to the domain {@link com.sporty.domain.EventOutcome} record and hands it to the
 * Kafka producer. Settlement happens asynchronously downstream — the controller returns
 * {@code 202 Accepted} as soon as the message is handed off, never waiting for consumers.
 */
@RestController
@RequestMapping("/api/v1/event-outcomes")
public class EventOutcomeController {

    private final EventOutcomeProducer producer;

    public EventOutcomeController(EventOutcomeProducer producer) {
        this.producer = producer;
    }

    /**
     * Publishes a sports-event outcome to the {@code event-outcomes} Kafka topic.
     *
     * @param request the outcome payload; rejected with {@code 400 Bad Request} if any
     *                required field is missing or blank
     * @return {@code 202 Accepted} once the message has been handed to the Kafka producer
     */
    @PostMapping
    public ResponseEntity<Void> publish(@Valid @RequestBody EventOutcomeRequest request) {
        producer.publish(request.toDomain());
        return ResponseEntity.accepted().build();
    }
}