package com.sporty.rocketmq;

import com.sporty.domain.BetSettlement;
import com.sporty.domain.BetStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BetSettlementProducerTest {

    @Mock
    private RocketMQTemplate rocketMQTemplate;

    @Test
    void sendsSettlementToConfiguredTopic() {
        BetSettlementProducer producer = new BetSettlementProducer(rocketMQTemplate, "bet-settlements");
        BetSettlement settlement = new BetSettlement(1L, 100L, 5L, BetStatus.WON, new BigDecimal("50.00"));

        producer.send(settlement);

        ArgumentCaptor<Message<?>> captor = ArgumentCaptor.forClass(Message.class);
        verify(rocketMQTemplate).syncSend(org.mockito.ArgumentMatchers.eq("bet-settlements"), captor.capture());
        assertThat(captor.getValue().getPayload()).isEqualTo(settlement);
    }
}
