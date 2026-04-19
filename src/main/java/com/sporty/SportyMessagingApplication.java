package com.sporty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the Sporty Messaging service.
 *
 * <p>Bootstraps the full application context: REST controllers, Kafka producer/consumer for
 * the {@code event-outcomes} topic, RocketMQ producer/consumer for the {@code bet-settlements}
 * topic, the in-memory H2 datasource and the JPA repositories.
 *
 * <p>The end-to-end flow exercised by this service is:
 * <pre>
 *   HTTP POST /api/v1/event-outcomes
 *        │
 *        ▼  (Kafka topic: event-outcomes)
 *   EventOutcomeConsumer ─▶ SettlementService ─▶ BetSettlementProducer
 *                                                       │
 *                                                       ▼  (RocketMQ topic: bet-settlements)
 *                                              BetSettlementConsumer ─▶ H2 (status updated)
 * </pre>
 */
@SpringBootApplication
public class SportyMessagingApplication {

    /**
     * Standard Spring Boot launcher.
     *
     * @param args command-line arguments forwarded to {@link SpringApplication#run}
     */

    public static void main(String[] args) {
        SpringApplication.run(SportyMessagingApplication.class, args);
    }
}