package com.sporty.rocketmq;

import com.sporty.domain.BetSettlement;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * Publishes {@link BetSettlement} messages to the RocketMQ {@code bet-settlements} topic.
 *
 * <p>Called by {@link com.sporty.service.SettlementService} once a pending bet has been
 * classified as {@code WON} or {@code LOST}. The corresponding
 * {@link BetSettlementConsumer} listens on the same topic and persists the final status.
 *
 * <p>This producer uses {@link RocketMQTemplate#syncSend(String, org.springframework.messaging.Message)}
 * so that broker failures surface immediately as exceptions rather than being silently
 * dropped — settlement is a financially significant operation where fire-and-forget is
 * inappropriate.
 */
@Component
public class BetSettlementProducer {

    private static final Logger log = LoggerFactory.getLogger(BetSettlementProducer.class);

    private final RocketMQTemplate rocketMQTemplate;
    private final String topic;

    public BetSettlementProducer(RocketMQTemplate rocketMQTemplate,
                                 @Value("${app.topics.bet-settlements}") String topic) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.topic = topic;
    }

    /**
     * Sends a settlement message to RocketMQ synchronously, waiting for broker
     * acknowledgement.
     *
     * @param settlement the settlement payload to publish
     * @throws org.springframework.messaging.MessagingException if the broker rejects the
     *                                                          message or times out
     */
    public void send(BetSettlement settlement) {
        log.info("Sending settlement to RocketMQ topic '{}': {}", topic, settlement);
        rocketMQTemplate.syncSend(topic, MessageBuilder.withPayload(settlement).build());
    }
}