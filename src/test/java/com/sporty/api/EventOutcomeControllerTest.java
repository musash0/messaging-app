package com.sporty.api;

import com.sporty.domain.EventOutcome;
import com.sporty.kafka.EventOutcomeProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventOutcomeController.class)
class EventOutcomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventOutcomeProducer producer;

    @Test
    void publishesEventOutcome() throws Exception {
        String body = """
                {
                  "eventId": 1,
                  "eventName": "Lakers vs Celtics",
                  "eventWinnerId": 42
                }
                """;

        mockMvc.perform(post("/api/v1/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isAccepted());

        verify(producer).publish(any(EventOutcome.class));
    }

    @Test
    void rejectsInvalidPayload() throws Exception {
        String body = """
                {
                  "eventName": "Missing IDs"
                }
                """;

        mockMvc.perform(post("/api/v1/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}