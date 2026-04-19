package com.sporty.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * JPA entity representing a bet placed by a user on a single outcome of a sports event.
 *
 * <p>A bet is created in {@link BetStatus#PENDING} and remains so until an event outcome
 * arrives on Kafka, at which point {@link com.sporty.service.SettlementService} classifies
 * it and the {@link com.sporty.rocketmq.BetSettlementConsumer} flips the status to
 * {@link BetStatus#WON} or {@link BetStatus#LOST}.
 *
 * <p>Persisted in the in-memory H2 schema managed by Hibernate (see {@code application.yml},
 * {@code ddl-auto=create-drop}).
 */
@Entity
@Table(name = "bets")
public class Bet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Long eventMarketId;

    @Column(nullable = false)
    private Long eventWinnerId;

    @Column(nullable = false)
    private BigDecimal betAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BetStatus status = BetStatus.PENDING;

    /**
     * Required no-arg constructor for JPA. Application code should not use this directly.
     */
    public Bet() {
    }

    /**
     * Creates a new pending bet.
     *
     * @param userId        identifier of the user who placed the bet
     * @param eventId       identifier of the sports event being bet on
     * @param eventMarketId identifier of the market within the event (e.g. match winner,
     *                      over/under, spread)
     * @param eventWinnerId identifier of the outcome the user is betting on; compared
     *                      against the actual event winner at settlement time
     * @param betAmount     stake amount; used as the basis for payout calculation on a win
     */
    public Bet(Long userId, Long eventId, Long eventMarketId, Long eventWinnerId, BigDecimal betAmount) {
        this.userId = userId;
        this.eventId = eventId;
        this.eventMarketId = eventMarketId;
        this.eventWinnerId = eventWinnerId;
        this.betAmount = betAmount;
        this.status = BetStatus.PENDING;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getEventId() {
        return eventId;
    }

    public Long getEventMarketId() {
        return eventMarketId;
    }

    public Long getEventWinnerId() {
        return eventWinnerId;
    }

    public BigDecimal getBetAmount() {
        return betAmount;
    }

    public BetStatus getStatus() {
        return status;
    }

    public void setStatus(BetStatus status) {
        this.status = status;
    }
}