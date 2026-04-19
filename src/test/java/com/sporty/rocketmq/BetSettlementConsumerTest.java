package com.sporty.rocketmq;

import com.sporty.domain.Bet;
import com.sporty.domain.BetSettlement;
import com.sporty.domain.BetStatus;
import com.sporty.repository.BetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BetSettlementConsumerTest {

    @Mock
    private BetRepository repository;

    @InjectMocks
    private BetSettlementConsumer consumer;

    @Test
    void persistsWonStatusForExistingBet() {
        Bet bet = bet(10L);
        when(repository.findById(10L)).thenReturn(Optional.of(bet));

        consumer.onMessage(new BetSettlement(10L, 1L, 5L, BetStatus.WON, new BigDecimal("50.00")));

        ArgumentCaptor<Bet> captor = ArgumentCaptor.forClass(Bet.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(BetStatus.WON);
    }

    @Test
    void persistsLostStatusForExistingBet() {
        Bet bet = bet(11L);
        when(repository.findById(11L)).thenReturn(Optional.of(bet));

        consumer.onMessage(new BetSettlement(11L, 1L, 5L, BetStatus.LOST, BigDecimal.ZERO));

        ArgumentCaptor<Bet> captor = ArgumentCaptor.forClass(Bet.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(BetStatus.LOST);
    }

    @Test
    void skipsSettlementWhenBetMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        consumer.onMessage(new BetSettlement(99L, 1L, 5L, BetStatus.WON, new BigDecimal("100")));

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private Bet bet(long id) {
        Bet b = new Bet(1L, 5L, 200L, 42L, new BigDecimal("25.00"));
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
