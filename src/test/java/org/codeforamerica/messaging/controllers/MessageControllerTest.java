package org.codeforamerica.messaging.controllers;


import org.codeforamerica.messaging.config.SecurityConfiguration;
import org.codeforamerica.messaging.models.Message;
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
    public void getMessageAuthenticated() throws Exception {
        Mockito.when(messageService.getMessage(any()))
                .thenReturn(Optional.ofNullable(Message.builder().id(1L).build()));

        mockMvc.perform(get("/api/v1/messages/1")
                        .with(httpBasic("user", "password")))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser
    public void createMessageSuccessSmsOnly() throws Exception {
        String requestBody = """
                        {
                        "toPhone": "1234567890",
                        "body": "This is a test"
                        }
                """;

        Message message = Message.builder().id(1L).build();
        Mockito.when(messageService.sendMessage(any()))
                .thenReturn(message);

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, endsWith("/messages/1")));
    }

//    @Test
//    public void createMessageSuccessEmailOnly() throws Exception {
//        String requestBody = """
//                        {
//                        "toEmail": "fake@email.com",
//                        "body": "This is a test",
//                        "subject": "Test"
//                        }
//                """;
//
//        Mockito.when(emailService.sendEmailMessage("fake@email.com", "This is a test", "Test"))
//                .thenReturn(EmailMessage.builder().providerMessageId("message_id").build());
//
//        mockMvc.perform(post("/api/v1/messages")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.content().string("Sent message(s)"));
//        Mockito.verify(smsService, Mockito.never()).sendSmsMessage(any(), any());
//        Mockito.verify(emailService, Mockito.times(1))
//                .sendEmailMessage("fake@email.com", "This is a test", "Test");
//    }
//
//    @Test
//    public void createMessageSuccessMultiModal() throws Exception {
//        String requestBody = """
//                        {
//                        "toPhone": "1234567890",
//                        "toEmail": "fake@email.com",
//                        "body": "This is a test",
//                        "subject": "Test"
//                        }
//                """;
//
//        Mockito.when(smsService.sendSmsMessage("1234567890", "This is a test"))
//                .thenReturn(Message.builder().providerMessageId("sms_id").build());
//        Mockito.when(emailService.sendEmailMessage("fake@email.com", "This is a test", "Test"))
//                .thenReturn(EmailMessage.builder().providerMessageId("email_id").build());
//
//        mockMvc.perform(post("/api/v1/messages")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.content().string("Sent message(s)"));
//        Mockito.verify(smsService, Mockito.times(1))
//                .sendSmsMessage("1234567890", "This is a test");
//        Mockito.verify(emailService, Mockito.times(1))
//                .sendEmailMessage("fake@email.com", "This is a test", "Test");
//>>>>>>> 09616a1 (WIP request)
//    }

    @Test
    @WithMockUser
    public void createMessageEmptyPayload() throws Exception {
        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Mockito.verify(messageService, Mockito.never()).sendMessage(any());
    }

    @Test
    @WithMockUser
    public void createMessageBadPhoneNumber() throws Exception {
        String requestBody = """
                        {
                        "toPhone": "A1234567890",
                        "body": "This is a test"
                        }
                """;

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Mockito.verify(messageService, Mockito.never()).sendMessage(any());
    }

    @Test
    @WithMockUser
    public void createMessageBadEmail() throws Exception {
        String requestBody = """
                        {
                        "toEmail": "not an email",
                        "body": "This is a test"
                        }
                """;

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Mockito.verify(messageService, Mockito.never()).sendMessage(any());
    }

    @Test
    @WithMockUser
    public void createMessageMissingSubject() throws Exception {
        String requestBody = """
                        {
                        "toEmail": "fake@email.com",
                        "body": "This is a test"
                        }
                """;

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Mockito.verify(messageService, Mockito.never()).sendMessage(any());
    }

    @Test
    @WithMockUser
    public void createMessageMissingPhoneAndEmail() throws Exception {
        String requestBody = """
                        {
                        "body": "This is a test"
                        }
                """;

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Mockito.verify(messageService, Mockito.never()).sendMessage(any());
    }

}
