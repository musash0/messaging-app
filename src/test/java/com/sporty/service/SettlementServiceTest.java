package com.sporty.service;

import com.sporty.domain.Bet;
import com.sporty.domain.BetSettlement;
import com.sporty.domain.BetStatus;
import com.sporty.domain.EventOutcome;
import com.sporty.repository.BetRepository;
import com.sporty.rocketmq.BetSettlementProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private BetRepository repository;

    @Mock
    private BetSettlementProducer producer;

    @InjectMocks
    private SettlementService service;

    @Test
    void emitsWonAndLostSettlementsForMatchingBets() {
        Bet winningBet = bet(1L, 42L, "10.00");
        Bet losingBet = bet(2L, 99L, "20.00");
        when(repository.findByEventIdAndStatus(1L, BetStatus.PENDING))
                .thenReturn(List.of(winningBet, losingBet));

        service.process(new EventOutcome(1L, "Lakers vs Celtics", 42L));

        ArgumentCaptor<BetSettlement> captor = ArgumentCaptor.forClass(BetSettlement.class);
        verify(producer, times(2)).send(captor.capture());

        List<BetSettlement> sent = captor.getAllValues();
        assertThat(sent).hasSize(2);
        assertThat(sent.get(0).outcome()).isEqualTo(BetStatus.WON);
        assertThat(sent.get(0).payout()).isEqualByComparingTo("20.00");
        assertThat(sent.get(1).outcome()).isEqualTo(BetStatus.LOST);
        assertThat(sent.get(1).payout()).isEqualByComparingTo("0");
    }

    @Test
    void doesNothingWhenNoPendingBets() {
        when(repository.findByEventIdAndStatus(7L, BetStatus.PENDING)).thenReturn(List.of());

        service.process(new EventOutcome(7L, "Empty", 1L));

        verify(producer, times(0)).send(org.mockito.ArgumentMatchers.any());
    }

    private Bet bet(long id, long winnerId, String amount) {
        Bet b = new Bet(100L + id, 1L, 200L, winnerId, new BigDecimal(amount));
        try {
            var idField = Bet.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(b, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return b;
    }
}