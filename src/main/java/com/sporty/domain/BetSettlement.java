package com.sporty.domain;

import java.math.BigDecimal;

/**
 * Message sent over the RocketMQ {@code bet-settlements} topic.
 *
 * <p>Produced by {@link com.sporty.service.SettlementService} once it has classified a
 * pending bet against an incoming event outcome, and consumed by
 * {@link com.sporty.rocketmq.BetSettlementConsumer}, which persists the final status to
 * the database.
 *
 * <p>Kept as an immutable record so the wire payload is unambiguous and safe to share
 * between producer and consumer.
 *
 * @param betId   identifier of the bet being settled
 * @param userId  identifier of the user who placed the bet (denormalised for downstream
 *                systems that may react to settlements without querying the bet table)
 * @param eventId identifier of the event this settlement is associated with
 * @param outcome final status to apply to the bet — {@link BetStatus#WON} or
 *                {@link BetStatus#LOST}; {@link BetStatus#PENDING} should never appear here
 * @param payout  amount to credit to the user on a win, or {@link BigDecimal#ZERO} on a loss
 */
public record BetSettlement(
        Long betId,
        Long userId,
        Long eventId,
        BetStatus outcome,
        BigDecimal payout) {
}