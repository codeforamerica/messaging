package org.codeforamerica.messaging.providers.mailgun;

import org.codeforamerica.messaging.TestData;
import org.codeforamerica.messaging.config.SecurityConfiguration;
import org.codeforamerica.messaging.jobs.EmailMessageStatusUpdateJobRequest;
import org.codeforamerica.messaging.models.EmailMessage;
import org.codeforamerica.messaging.models.Message;
import org.codeforamerica.messaging.models.MessageStatus;
import org.codeforamerica.messaging.repositories.EmailMessageRepository;
import org.codeforamerica.messaging.repositories.EmailSubscriptionRepository;
import org.codeforamerica.messaging.services.EmailService;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(MailgunCallbackController.class)
@Import(SecurityConfiguration.class)
@TestPropertySource(properties = {"server.trustedPort=80"})
public class MailgunCallbackControllerTest {

    @MockBean
    EmailMessageRepository emailMessageRepository;
    @MockBean
    MailgunSignatureVerificationService mailgunSignatureVerificationService;
    @MockBean
    EmailSubscriptionRepository emailSubscriptionRepository;
    @MockBean
    JobRequestScheduler jobRequestScheduler;
    @MockBean
    EmailService emailService;
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void whenTrustedPortAndSignatureNotVerified_ThenUnauthorized() throws Exception {
        Mockito.when(emailMessageRepository.findFirstByProviderMessageId(TestData.PROVIDER_MESSAGE_ID))
                .thenReturn(TestData.anEmailMessage().build());
        Mockito.when(mailgunSignatureVerificationService.verifySignature(any())).thenReturn(false);

        mockMvc.perform(post("/public/mailgun_callbacks/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "event-data": {
                                            "event": "delivered",
                                            "message": {
                                              "headers": {
                                                "message-id": "%s"
                                              }
                                            }
                                        }
                                    }
                                """.formatted(TestData.PROVIDER_MESSAGE_ID)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void whenTrustedPortAndSignatureVerified_ThenSucceeds() throws Exception {
        Message message = TestData.aMessage(TestData.aTemplateVariant().build()).build();
        EmailMessage emailMessage = TestData.anEmailMessage().message(message).build();
        Mockito.when(emailMessageRepository.findFirstByProviderMessageId(TestData.PROVIDER_MESSAGE_ID))
                .thenReturn(emailMessage);
        Mockito.when(mailgunSignatureVerificationService.verifySignature(any())).thenReturn(true);

        mockMvc.perform(post("/public/mailgun_callbacks/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "event-data": {
                                            "event": "delivered",
                                            "message": {
                                              "headers": {
                                                "message-id": "%s"
                                              }
                                            }
                                        }
                                    }
                                """.formatted(TestData.PROVIDER_MESSAGE_ID)))
                .andExpect(MockMvcResultMatchers.status().isOk());
        }

    @Test
    public void whenNewStatusUndelivered_ThenEnqueueStatusUpdateJobWithProviderError() throws Exception {
        String severity = "permanent";
        String reason = "bounce";
        String errorCode = "550";
        String errorMessage = "5.1.1 The email account that you tried to reach does not exist";
        String errorDescription = "";

        Message message = TestData.aMessage(TestData.aTemplateVariant().build()).build();
        EmailMessage emailMessage = TestData.anEmailMessage().message(message).build();
        Mockito.when(emailMessageRepository.findFirstByProviderMessageId(TestData.PROVIDER_MESSAGE_ID))
                .thenReturn(emailMessage);
        Mockito.when(mailgunSignatureVerificationService.verifySignature(any())).thenReturn(true);

        mockMvc.perform(post("/public/mailgun_callbacks/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "event-data": {
                                            "event": "failed",
                                            "severity": "%s",
                                            "reason": "%s",
                                            "message": {
                                              "headers": {
                                                "message-id": "%s"
                                              }
                                            },
                                            "delivery-status": {
                                                "code": "%s",
                                                "message": "%s",
                                                "description" : "%s"
                                            }
                                        }
                                    }
                                """.formatted(severity, reason, TestData.PROVIDER_MESSAGE_ID, errorCode, errorMessage, errorDescription)))
                .andExpect(MockMvcResultMatchers.status().isOk());
        ArgumentCaptor<EmailMessageStatusUpdateJobRequest> emailMessageStatusUpdateJobRequestCaptor = ArgumentCaptor.forClass(EmailMessageStatusUpdateJobRequest.class);
        Mockito.verify(jobRequestScheduler).enqueue(emailMessageStatusUpdateJobRequestCaptor.capture());
        assertEquals(TestData.PROVIDER_MESSAGE_ID, emailMessageStatusUpdateJobRequestCaptor.getValue().getProviderMessageId());
        assertEquals("failed", emailMessageStatusUpdateJobRequestCaptor.getValue().getRawStatus());
        assertEquals(MessageStatus.undelivered, emailMessageStatusUpdateJobRequestCaptor.getValue().getMessageStatus());
        assertEquals(Map.of("severity", "permanent", "reason", "bounce",
                        "errorCode", "550", "errorMessage", "5.1.1 The email account that you tried to reach does not exist",
                        "errorDescription", ""),
                emailMessageStatusUpdateJobRequestCaptor.getValue().getProviderError());
    }

    @Test
    public void whenNewStatusAccepted_ThenEnqueueStatusUpdateJobWithNoProviderError() throws Exception {
        String newStatus = "accepted";
        Message message = TestData.aMessage(TestData.aTemplateVariant().build()).emailStatus(MessageStatus.delivered).build();
        EmailMessage emailMessage = TestData.anEmailMessage().message(message).build();
        Mockito.when(emailMessageRepository.findFirstByProviderMessageId(TestData.PROVIDER_MESSAGE_ID))
                .thenReturn(emailMessage);
        Mockito.when(mailgunSignatureVerificationService.verifySignature(any())).thenReturn(true);

        mockMvc.perform(post("/public/mailgun_callbacks/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                    {
                                        "event-data": {
                                            "event": "%s",
                                            "message": {
                                              "headers": {
                                                "message-id": "%s"
                                              }
                                            }
                                        }
                                    }
                                """.formatted(newStatus, TestData.PROVIDER_MESSAGE_ID)))
                .andExpect(MockMvcResultMatchers.status().isOk());
        ArgumentCaptor<EmailMessageStatusUpdateJobRequest> emailMessageStatusUpdateJobRequestCaptor = ArgumentCaptor.forClass(EmailMessageStatusUpdateJobRequest.class);
        Mockito.verify(jobRequestScheduler).enqueue(emailMessageStatusUpdateJobRequestCaptor.capture());
        assertEquals(TestData.PROVIDER_MESSAGE_ID, emailMessageStatusUpdateJobRequestCaptor.getValue().getProviderMessageId());
        assertEquals("accepted", emailMessageStatusUpdateJobRequestCaptor.getValue().getRawStatus());
        assertEquals(MessageStatus.queued, emailMessageStatusUpdateJobRequestCaptor.getValue().getMessageStatus());
        assertNull(emailMessageStatusUpdateJobRequestCaptor.getValue().getProviderError());
    }

    @Test
    public void whenUnsubscribed_ThenSavesEmailSubscription() throws Exception {
        String recipient = "unsubscriber@example.com";

        Message message = TestData.aMessage(TestData.aTemplateVariant().build()).build();
        EmailMessage emailMessage = TestData.anEmailMessage().message(message).build();
        Mockito.when(emailMessageRepository.findFirstByProviderMessageId(TestData.PROVIDER_MESSAGE_ID))
                .thenReturn(emailMessage);
        Mockito.when(mailgunSignatureVerificationService.verifySignature(any())).thenReturn(true);

        mockMvc.perform(post("/public/mailgun_callbacks/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "event-data": {
                                            "event": "unsubscribed",
                                            "recipient": "%s",
                                            "message": {
                                                  "headers": {
                                                    "message-id": "%s"
                                                  }
                                            }
                                        }
                                    }
                                """.formatted(recipient, TestData.PROVIDER_MESSAGE_ID)))
                .andExpect(MockMvcResultMatchers.status().isOk());
        Mockito.verify(emailService).unsubscribe(recipient);
    }
}
