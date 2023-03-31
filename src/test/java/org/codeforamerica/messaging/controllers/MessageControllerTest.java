package org.codeforamerica.messaging.controllers;

import org.codeforamerica.messaging.models.Message;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.services.SmsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(MessageController.class)
public class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SmsService smsService;
    @MockBean
    private MessageRepository messageRepository;

    @Test
    public void createMessageSuccess() throws Exception {
        String requestBody = """
                        {
                        "to": "1234567890",
                        "body": "This is a test"
                        }
                """;

        Mockito.when(smsService.sendSmsMessage("1234567890", "This is a test"))
                .thenReturn(Message.builder().providerMessageId("message_id").build());

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(containsString("message_id")));
    }

    @Test
    public void createMessageEmptyPayload() throws Exception {
        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Mockito.verify(smsService, Mockito.never()).sendSmsMessage("1234567890", "This is a test");
    }

    @Test
    public void createMessageBadPhoneNumber() throws Exception {
        String requestBody = """
                        {
                        "to": "A1234567890",
                        "body": "This is a test"
                        }
                """;

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Mockito.verify(smsService, Mockito.never()).sendSmsMessage("1234567890", "This is a test");
    }

}
