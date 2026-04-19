package com.sporty.rocketmq;

import com.sporty.domain.Bet;
import com.sporty.domain.BetSettlement;
import com.sporty.repository.BetRepository;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * RocketMQ listener on the {@code bet-settlements} topic that finalises bet status in the
 * database.
 *
 * <p>This class is the last step in the settlement pipeline — the place where the bet's
 * lifecycle transitions from {@code PENDING} to its terminal state. Because this write is
 * financially significant, the handler is {@link Transactional}: a thrown exception rolls
 * back the update and causes RocketMQ to redeliver the message according to its retry
 * policy.
 *
 * <p>If the referenced bet is not found (e.g. it was deleted between enqueue and delivery),
 * the message is acknowledged without effect to avoid retry loops on permanently missing
 * data. In a production system this branch would typically emit a dead-letter or alert.
 */
@Component
@RocketMQMessageListener(
        topic = "${app.topics.bet-settlements}",
        consumerGroup = "bet-settlement-consumer"
)
public class BetSettlementConsumer implements RocketMQListener<BetSettlement> {

    private static final Logger log = LoggerFactory.getLogger(BetSettlementConsumer.class);

    private final BetRepository repository;

    public BetSettlementConsumer(BetRepository repository) {
        this.repository = repository;
    }

    /**
     * Applies an incoming settlement to the stored bet.
     *
     * @param settlement the settlement message; its {@link BetSettlement#outcome()} is
     *                   copied onto the bet as its new {@link com.sporty.domain.BetStatus}
     */
    @Override
    @Transactional
    public void onMessage(BetSettlement settlement) {
        log.info("Received settlement from RocketMQ: {}", settlement);
        Bet bet = repository.findById(settlement.betId()).orElse(null);
        if (bet == null) {
            log.warn("Bet {} not found, skipping settlement", settlement.betId());
            return;
        }
        bet.setStatus(settlement.outcome());
        repository.save(bet);
        log.info("Bet {} settled as {} (payout={})", bet.getId(), settlement.outcome(), settlement.payout());
    }
}