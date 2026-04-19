package com.sporty.repository;

import com.sporty.domain.Bet;
import com.sporty.domain.BetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Bet}.
 *
 * <p>Inherits the full CRUD surface from {@link JpaRepository} and adds a single derived
 * query that powers the settlement flow: "find all pending bets for this event".
 */
public interface BetRepository extends JpaRepository<Bet, Long> {

    /**
     * Finds every bet on the given event that is still awaiting settlement.
     *
     * <p>Called by {@link com.sporty.service.SettlementService} on each incoming event
     * outcome. The query is derived from the method name — no custom JPQL required.
     *
     * @param eventId identifier of the event whose bets should be settled
     * @param status  lifecycle state to filter by; in practice always {@link BetStatus#PENDING}
     * @return matching bets, or an empty list if there are none
     */
    List<Bet> findByEventIdAndStatus(Long eventId, BetStatus status);
}