package org.codeforamerica.messaging.controllers;


import org.codeforamerica.messaging.TestData;
import org.codeforamerica.messaging.config.SecurityConfiguration;
import org.codeforamerica.messaging.models.Message;
import org.codeforamerica.messaging.models.MessageRequest;
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
        mockMvc.perform(get("/api/v1/messages/" + TestData.BASE_ID))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void whenAuthenticatedAndPhoneAndNoAssociatedSmsMessage_ThenStatusPending() throws Exception {
        String expectedResponse = """
                {
                    id: %s,
                    status: "pending",
                    toPhone: "%s"
                }
                """.formatted(TestData.BASE_ID, TestData.TO_PHONE);

        Mockito.when(messageService.getMessage(any()))
                .thenReturn(Optional.of(TestData.aMessage().toPhone(TestData.TO_PHONE).build()));

        mockMvc.perform(get("/api/v1/messages/" + TestData.BASE_ID)
                        .with(httpBasic(TestData.USERNAME, TestData.PASSWORD)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect((MockMvcResultMatchers.content().json(expectedResponse)));
    }

    @Test
    @WithMockUser
    public void whenAuthenticatedAndPhoneAndHasAssociatedSmsMessage_ThenStatusCompleted() throws Exception {
        String expectedResponse = """
                {
                    id: %s,
                    status: "completed",
                    toPhone: "%s"
                }
                """.formatted(TestData.BASE_ID, TestData.TO_PHONE);

        Mockito.when(messageService.getMessage(any()))
                .thenReturn(Optional.of(TestData.aMessage()
                        .toPhone(TestData.TO_PHONE)
                        .smsMessage(TestData.anSmsMessage().build())
                        .build()));

        mockMvc.perform(get("/api/v1/messages/" + TestData.BASE_ID)
                        .with(httpBasic(TestData.USERNAME, TestData.PASSWORD)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect((MockMvcResultMatchers.content().json(expectedResponse)));
    }

    @Test
    @WithMockUser
    public void createMessageSuccessSmsOnly() throws Exception {
        String requestBody = """
                {
                    "toPhone": "%s",
                    "templateName": "%s"
                }
                """.formatted(TestData.TO_PHONE, TestData.TEMPLATE_NAME);

        Mockito.when(messageService.scheduleMessage(any()))
                .thenReturn(TestData.aMessage().build());

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, endsWith("/messages/" + TestData.BASE_ID)));
    }

    @Test
    @WithMockUser
    public void createMessageSuccessEmailOnly() throws Exception {
        String requestBody = """
                {
                    "toEmail": "%s",
                    "templateName": "%s"
                }
                """.formatted(TestData.TO_EMAIL, TestData.TEMPLATE_NAME);

        Mockito.when(messageService.scheduleMessage(any()))
                .thenReturn(TestData.aMessage().build());

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, endsWith("/messages/" + TestData.BASE_ID)));
    }

    @Test
    @WithMockUser
    public void createMessageSuccessMultiModal() throws Exception {
        String requestBody = """
                {
                    "toPhone": "%s",
                    "toEmail": "%s",
                    "templateName": "%s"
                }
                """.formatted(TestData.TO_PHONE, TestData.TO_EMAIL, TestData.TEMPLATE_NAME);

        Mockito.when(messageService.scheduleMessage(any()))
                .thenReturn(TestData.aMessage().build());

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, endsWith("/messages/" + TestData.BASE_ID)));
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
                    "templateName": "%s"
                }
                """.formatted(TestData.TEMPLATE_NAME);

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
                    "templateName": "%s"
                }
                """.formatted(TestData.TO_EMAIL);

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
                    "templateName": "%s"
                }
                """.formatted(TestData.TEMPLATE_NAME);

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
                    "toEmail": "%s",
                    "templateName": "%s",
                    "invalid_field": "1234567890"
                }
                """.formatted(TestData.TO_EMAIL, TestData.TEMPLATE_NAME);

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
                    "toEmail": "%s",
                    "templateName": "%s",
                    "templateParams": {
                        "language": "es",
                        "treatment": "B"
                    }
                }
                """.formatted(TestData.TO_EMAIL, TestData.TEMPLATE_NAME);

        Message message = TestData.aMessage().build();
        MessageRequest expectedMessageRequest = TestData.aMessageRequest()
                .toEmail(TestData.TO_EMAIL)
                .templateParams(Map.of(
                        "language", "es",
                        "treatment", "B"))
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
