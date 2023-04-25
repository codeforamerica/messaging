package org.codeforamerica.messaging.controllers;

import org.codeforamerica.messaging.config.SecurityConfiguration;
import org.codeforamerica.messaging.models.SmsMessage;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.services.SmsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(MessageController.class)
@Import(SecurityConfiguration.class)
public class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SmsService smsService;
    @MockBean
    private MessageRepository messageRepository;

    @Test
    public void getMessageUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/messages/1"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void getMessageAuthenticated() throws Exception {
        Mockito.when(smsService.getMessage(any()))
                .thenReturn(Optional.of(SmsMessage.builder().providerMessageId("message_id").build()));

        mockMvc.perform(get("/api/v1/messages/1")
                        .with(httpBasic("user", "password")))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser
    public void createMessageSuccess() throws Exception {
        String requestBody = """
                        {
                        "to": "1234567890",
                        "body": "This is a test"
                        }
                """;

        Mockito.when(smsService.sendSmsMessage("1234567890", "This is a test"))
                .thenReturn(SmsMessage.builder().providerMessageId("message_id").build());

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(containsString("message_id")));
    }

    @Test
    @WithMockUser
    public void createMessageEmptyPayload() throws Exception {
        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Mockito.verify(smsService, Mockito.never()).sendSmsMessage("1234567890", "This is a test");
    }

    @Test
    @WithMockUser
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
