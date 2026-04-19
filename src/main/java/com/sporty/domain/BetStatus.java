package com.sporty.domain;

/**
 * Lifecycle state of a {@link Bet}.
 *
 * <p>A bet starts in {@link #PENDING} and transitions to a terminal state exactly once,
 * when the matching event outcome is processed by the settlement pipeline.
 */
public enum BetStatus {
    /** Bet has been placed but the event has not yet been settled. Initial state. */
    PENDING,

    /** Event winner matches the bet's picked winner; the user is owed a payout. Terminal. */
    WON,

    /** Event winner does not match the bet's picked winner; no payout is owed. Terminal. */
    LOST
}