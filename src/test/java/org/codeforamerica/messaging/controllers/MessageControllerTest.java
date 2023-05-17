package org.codeforamerica.messaging.controllers;


import org.codeforamerica.messaging.config.SecurityConfiguration;
import org.codeforamerica.messaging.models.Message;
import org.codeforamerica.messaging.models.MessageRequest;
import org.codeforamerica.messaging.models.SmsMessage;
import org.codeforamerica.messaging.services.MessageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.endsWith;
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
    private MessageService messageService;

    @Test
    public void getMessageUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/messages/1"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void whenAuthenticatedAndPhoneAndNoAssociatedSmsMessage_ThenStatusPending() throws Exception {
        String expectedResponse = """
                {
                    id: 1,
                    status: "pending",
                    toPhone: "1234567890"
                }
                """;
        Message message = Message.builder()
                .id(1L)
                .toPhone("1234567890")
                .build();

        Mockito.when(messageService.getMessage(any()))
                .thenReturn(Optional.of(message));

        mockMvc.perform(get("/api/v1/messages/1")
                        .with(httpBasic("user", "password")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect((MockMvcResultMatchers.content().json(expectedResponse)));
    }

    @Test
    @WithMockUser
    public void whenAuthenticatedAndPhoneAndHasAssociatedSmsMessage_ThenStatusCompleted() throws Exception {
        String expectedResponse = """
                {
                    id: 1,
                    status: "completed",
                    toPhone: "1234567890"
                }
                """;
        Message message = Message.builder()
                .id(1L)
                .toPhone("1234567890")
                .build();
        SmsMessage smsMessage = SmsMessage.builder()
                .build();
        message.setSmsMessage(smsMessage);

        Mockito.when(messageService.getMessage(any()))
                .thenReturn(Optional.of(message));

        mockMvc.perform(get("/api/v1/messages/1")
                        .with(httpBasic("user", "password")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect((MockMvcResultMatchers.content().json(expectedResponse)));
    }

    @Test
    @WithMockUser
    public void createMessageSuccessSmsOnly() throws Exception {
        String requestBody = """
                {
                    "toPhone": "1234567890",
                    "templateName": "test"
                }
                """;

        Message message = Message.builder().id(1L).build();
        Mockito.when(messageService.scheduleMessage(any()))
                .thenReturn(message);

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, endsWith("/messages/1")));
    }

    @Test
    @WithMockUser
    public void createMessageSuccessEmailOnly() throws Exception {
        String requestBody = """
                {
                    "toEmail": "fake@email.com",
                    "templateName": "test"
                }
                """;

        Message message = Message.builder().id(1L).build();
        Mockito.when(messageService.scheduleMessage(any()))
                .thenReturn(message);

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, endsWith("/messages/1")));
    }

    @Test
    @WithMockUser
    public void createMessageSuccessMultiModal() throws Exception {
        String requestBody = """
                {
                    "toPhone": "1234567890",
                    "toEmail": "fake@email.com",
                    "templateName": "test"
                }
                """;

        Message message = Message.builder().id(1L).build();
        Mockito.when(messageService.scheduleMessage(any()))
                .thenReturn(message);

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, endsWith("/messages/1")));
    }

    @Test
    @WithMockUser
    public void createMessageEmptyPayload() throws Exception {
        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Mockito.verify(messageService, Mockito.never()).scheduleMessage(any());
    }

    @Test
    @WithMockUser
    public void createMessageBadPhoneNumber() throws Exception {
        String requestBody = """
                {
                    "toPhone": "A1234567890",
                    "templateName": "test"
                }
                """;

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Mockito.verify(messageService, Mockito.never()).scheduleMessage(any());
    }

    @Test
    @WithMockUser
    public void createMessageBadEmail() throws Exception {
        String requestBody = """
                {
                    "toEmail": "not an email",
                    "templateName": "test"
                }
                """;

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Mockito.verify(messageService, Mockito.never()).scheduleMessage(any());
    }

    @Test
    @WithMockUser
    public void createMessageMissingPhoneAndEmail() throws Exception {
        String requestBody = """
                {
                    "templateName": "test"
                }
                """;

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Mockito.verify(messageService, Mockito.never()).scheduleMessage(any());
    }

    @Test
    @WithMockUser
    public void whenExtraFieldsPresent_RejectsAsBadRequest() throws Exception {
        String requestBody = """
                {
                    "toEmail": "fake@email.com",
                    "templateName": "test",
                    "invalid_field": "1234567890"
                }
                """;

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Mockito.verify(messageService, Mockito.never()).scheduleMessage(any());
    }

    @Test
    @WithMockUser
    public void whenTemplateParamsPresent_thenParseParams() throws Exception {
        String requestBody = """
                {
                    "toEmail": "fake@email.com",
                    "templateName": "test",
                    "templateParams": {
                        "language": "es",
                        "treatment": "B"
                    }
                }
                """;

        Message message = Message.builder().id(1L).build();
        MessageRequest expectedMessageRequest = MessageRequest.builder()
                .toEmail("fake@email.com")
                .templateName("test")
                .templateParams(Map.of("language", "es", "treatment", "B"))
                .build();
        Mockito.when(messageService.scheduleMessage(expectedMessageRequest))
                .thenReturn(message);

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, endsWith("/messages/1")));
    }

}
