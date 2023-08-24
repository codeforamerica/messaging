package org.codeforamerica.messaging.providers.mailgun;

import org.codeforamerica.messaging.TestData;
import org.codeforamerica.messaging.config.SecurityConfiguration;
import org.codeforamerica.messaging.models.EmailMessage;
import org.codeforamerica.messaging.models.EmailSubscription;
import org.codeforamerica.messaging.models.Message;
import org.codeforamerica.messaging.models.MessageStatus;
import org.codeforamerica.messaging.repositories.EmailMessageRepository;
import org.codeforamerica.messaging.repositories.EmailSubscriptionRepository;
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

import static org.junit.jupiter.api.Assertions.*;
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
    @Autowired
    private MockMvc mockMvc;

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
        assertEquals(MessageStatus.delivered, emailMessage.getMessage().getEmailStatus());
    }

    @Test
    public void whenFailed_ThenSavesProviderError() throws Exception {
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
        assertEquals(Map.of("severity", severity, "reason", reason, "errorCode", errorCode, "errorMessage", errorMessage, "errorDescription", errorDescription), emailMessage.getProviderError());
    }

    @Test
    public void whenNewStatusIsBeforeCurrentStatus_ThenIgnore() throws Exception {
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
        assertNotEquals(MessageStatus.queued, emailMessage.getMessage().getEmailStatus());
    }

    @Test
    public void whenNewStatusIsAfterCurrentStatus_ThenUpdateStatus() throws Exception {
        String newStatus = "delivered";
        Message message = TestData.aMessage(TestData.aTemplateVariant().build()).emailStatus(MessageStatus.queued).build();
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
        assertEquals(MessageStatus.delivered, emailMessage.getMessage().getEmailStatus());
    }

    @Test
    public void whenNoCurrentStatus_ThenUpdateStatus() throws Exception {
        String newStatus = "failed";
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
                                            "event": "%s",
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
                                """.formatted(newStatus, severity, reason, TestData.PROVIDER_MESSAGE_ID, errorCode, errorMessage, errorDescription)))
                .andExpect(MockMvcResultMatchers.status().isOk());
        assertEquals(newStatus, emailMessage.getMessage().getRawEmailStatus());
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
        ArgumentCaptor<EmailSubscription> emailSubscriptionCaptor = ArgumentCaptor.forClass(EmailSubscription.class);
        Mockito.verify(emailSubscriptionRepository).save(emailSubscriptionCaptor.capture());
        EmailSubscription emailSubscription = emailSubscriptionCaptor.getValue();
        assertTrue(emailSubscription.isUnsubscribed());
        assertEquals(emailSubscription.getEmail(), recipient);
        assertTrue(emailSubscription.isSourceInternal());
        assertNotEquals(MessageStatus.unsubscribed, message.getEmailStatus());
    }

    @Test
    public void whenTrustedPortAndSignatureNotVerified_ThenFails() throws Exception {
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
}
