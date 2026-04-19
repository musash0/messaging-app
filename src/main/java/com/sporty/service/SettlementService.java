package com.sporty.service;

import com.sporty.domain.Bet;
import com.sporty.domain.BetSettlement;
import com.sporty.domain.BetStatus;
import com.sporty.domain.EventOutcome;
import com.sporty.repository.BetRepository;
import com.sporty.rocketmq.BetSettlementProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Core domain service: matches pending bets to incoming event outcomes and emits the
 * resulting settlement messages.
 *
 * <p>Invoked by the Kafka consumer {@link com.sporty.kafka.EventOutcomeConsumer}. For each
 * incoming {@link EventOutcome} the service:
 * <ol>
 *   <li>Loads every {@link Bet} on the same event still in {@link BetStatus#PENDING}.</li>
 *   <li>Classifies each bet as {@link BetStatus#WON} (bet's picked winner matches the event
 *       winner) or {@link BetStatus#LOST} (otherwise).</li>
 *   <li>Computes the payout — {@code betAmount * WIN_MULTIPLIER} on a win, zero on a loss.</li>
 *   <li>Sends the resulting {@link BetSettlement} to RocketMQ via
 *       {@link BetSettlementProducer}.</li>
 * </ol>
 *
 * <p>Deliberately does <strong>not</strong> update the bet's status in the database here —
 * that write happens in {@link com.sporty.rocketmq.BetSettlementConsumer} once the
 * settlement message is delivered. Splitting the concerns keeps this service pure (easy to
 * unit test) and matches the assignment's explicit two-stage Kafka-then-RocketMQ pipeline.
 *
 * <p>The payout multiplier is a constant here; a real odds engine would inject per-market
 * odds at bet-placement time and consult them at settlement.
 */
@Service
public class SettlementService {

    private static final Logger log = LoggerFactory.getLogger(SettlementService.class);
    private static final BigDecimal WIN_MULTIPLIER = new BigDecimal("2");

    private final BetRepository repository;
    private final BetSettlementProducer producer;

    public SettlementService(BetRepository repository, BetSettlementProducer producer) {
        this.repository = repository;
        this.producer = producer;
    }

    /**
     * Entry point for processing a single event outcome.
     *
     * <p>Loads all pending bets on the outcome's event, classifies each, and emits a
     * {@link BetSettlement} message per bet. Bets on other events are not touched.
     *
     * @param outcome the event outcome that just arrived from Kafka
     */
    public void process(EventOutcome outcome) {
        List<Bet> pendingBets = repository.findByEventIdAndStatus(outcome.eventId(), BetStatus.PENDING);
        log.info("Found {} pending bets for eventId={}", pendingBets.size(), outcome.eventId());

        for (Bet bet : pendingBets) {
            BetSettlement settlement = settle(bet, outcome);
            producer.send(settlement);
        }
    }

    /**
     * Classifies a single bet against an event outcome and builds the corresponding
     * settlement message. Package-private to allow direct unit testing.
     *
     * @param bet     a pending bet on the same event as {@code outcome}
     * @param outcome the event outcome being applied
     * @return a {@link BetSettlement} ready to send to RocketMQ
     */
    BetSettlement settle(Bet bet, EventOutcome outcome) {
        boolean won = Objects.equals(bet.getEventWinnerId(), outcome.eventWinnerId());
        BetStatus result = won ? BetStatus.WON : BetStatus.LOST;
        BigDecimal payout = won ? bet.getBetAmount().multiply(WIN_MULTIPLIER) : BigDecimal.ZERO;
        return new BetSettlement(bet.getId(), bet.getUserId(), bet.getEventId(), result, payout);
    }
}