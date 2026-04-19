package com.sporty.bootstrap;

import com.sporty.domain.Bet;
import com.sporty.repository.BetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Startup helper that populates the in-memory H2 database with demo bets.
 *
 * <p>The database is configured with {@code ddl-auto=create-drop}, so every boot starts
 * with an empty schema. Without seed data, the README's example {@code curl} call would
 * match zero pending bets and the Kafka → settlement → RocketMQ flow would produce nothing
 * observable. This class inserts a small, deterministic fixture calibrated to the demo:
 *
 * <ul>
 *   <li>3 pending bets for {@code eventId=1} — two with winner {@code 42} (will become
 *       {@code WON} when an outcome with {@code eventWinnerId=42} is published) and one
 *       with winner {@code 99} (will become {@code LOST}).</li>
 *   <li>1 pending bet for {@code eventId=2} — left untouched by the demo, useful for
 *       asserting that unrelated bets are not incorrectly settled.</li>
 * </ul>
 *
 * <p>In a production service this would be replaced by a proper bet-placement endpoint or
 * a separate betting service; it exists purely so the take-home demo is self-contained.
 */
@Component
public class SampleBetSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SampleBetSeeder.class);

    private final BetRepository repository;

    public SampleBetSeeder(BetRepository repository) {
        this.repository = repository;
    }

    /**
     * Invoked once by Spring Boot after the application context has started.
     *
     * @param args command-line arguments (unused)
     */
    @Override
    public void run(String... args) {
        List<Bet> bets = List.of(
                new Bet(1001L, 1L, 100L, 42L, new BigDecimal("25.00")),
                new Bet(1002L, 1L, 100L, 99L, new BigDecimal("10.00")),
                new Bet(1003L, 1L, 101L, 42L, new BigDecimal("50.00")),
                new Bet(1004L, 2L, 200L, 7L, new BigDecimal("15.00"))
        );
        repository.saveAll(bets);
        log.info("Seeded {} sample bets", bets.size());
    }
}