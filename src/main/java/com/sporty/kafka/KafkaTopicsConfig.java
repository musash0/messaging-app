package com.sporty.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declares Kafka topics owned by this service.
 *
 * <p>Spring's {@code KafkaAdmin} picks up every {@link NewTopic} bean on startup and creates
 * the topic on the broker if it does not yet exist. This avoids relying on broker-side
 * auto-create behaviour and makes the topic's configuration (partitions, replication)
 * visible in code.
 *
 * <p>Only {@code event-outcomes} is declared here. The RocketMQ topic {@code bet-settlements}
 * is created implicitly when the producer first sends to it (auto-create is enabled in the
 * broker configuration provided by {@code docker-compose.yml}).
 */
@Configuration
public class KafkaTopicsConfig {

    /**
     * Defines the {@code event-outcomes} topic with a single partition and single replica —
     * sufficient for the local single-broker KRaft setup. Production deployments would
     * increase these values.
     *
     * @param topic topic name injected from {@code app.topics.event-outcomes}
     * @return a {@link NewTopic} definition consumed by Spring's {@code KafkaAdmin}
     */
    @Bean
    public NewTopic eventOutcomesTopic(@Value("${app.topics.event-outcomes}") String topic) {
        return TopicBuilder.name(topic).partitions(1).replicas(1).build();
    }
}