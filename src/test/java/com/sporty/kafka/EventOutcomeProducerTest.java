package com.sporty.kafka;

import com.sporty.domain.EventOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventOutcomeProducerTest {

    @Mock
    private KafkaTemplate<String, EventOutcome> kafkaTemplate;

    @Test
    void publishesToConfiguredTopicKeyedByEventId() {
        EventOutcomeProducer producer = new EventOutcomeProducer(kafkaTemplate, "event-outcomes");
        EventOutcome outcome = new EventOutcome(7L, "Lakers vs Celtics", 42L);

        producer.publish(outcome);

        verify(kafkaTemplate).send("event-outcomes", "7", outcome);
    }
}
