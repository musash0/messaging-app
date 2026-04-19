package com.sporty.api;

import com.sporty.domain.Bet;
import com.sporty.repository.BetRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only verification endpoint for inspecting bet state.
 *
 * <p>Not part of the core settlement flow — this controller exists so that reviewers and
 * integration tests can confirm end-to-end behaviour (i.e. that seeded bets transition from
 * {@code PENDING} to {@code WON} / {@code LOST} after an event outcome is published) without
 * needing to open the H2 console.
 *
 * <p>In a production system the equivalent endpoint would typically be paginated, filtered
 * by user and protected by authentication.
 */
@RestController
@RequestMapping("/api/v1/bets")
public class BetController {

    private final BetRepository repository;

    public BetController(BetRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns every bet currently stored in the in-memory database.
     *
     * @return all bets, in no guaranteed order
     */
    @GetMapping
    public List<Bet> all() {
        return repository.findAll();
    }
}