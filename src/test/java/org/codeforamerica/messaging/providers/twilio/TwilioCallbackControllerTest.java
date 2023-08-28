package org.codeforamerica.messaging.providers.twilio;

import org.codeforamerica.messaging.TestData;
import org.codeforamerica.messaging.config.SecurityConfiguration;
import org.codeforamerica.messaging.jobs.SmsMessageStatusUpdateJobRequest;
import org.codeforamerica.messaging.models.Message;
import org.codeforamerica.messaging.models.MessageStatus;
import org.codeforamerica.messaging.models.SmsMessage;
import org.codeforamerica.messaging.repositories.SmsMessageRepository;
import org.codeforamerica.messaging.services.SmsService;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

@WebMvcTest(TwilioCallbackController.class)
@Import(SecurityConfiguration.class)
@TestPropertySource(properties = {"server.trustedPort=80"})
public class TwilioCallbackControllerTest {

    @MockBean
    SmsMessageRepository smsMessageRepository;
    @MockBean
    JobRequestScheduler jobRequestScheduler;
    @MockBean
    TwilioSignatureVerificationService twilioSignatureVerificationService;
    @MockBean
    SmsService smsService;
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void whenTrustedPortAndSignatureNotVerified_ThenUnauthorized() throws Exception {
        Mockito.when(smsMessageRepository.findFirstByProviderMessageId(TestData.PROVIDER_MESSAGE_ID))
                .thenReturn(TestData.anSmsMessage().build());
        Mockito.when(twilioSignatureVerificationService.verifySignature(any())).thenReturn(false);

        mockMvc.perform(post("/public/twilio_callbacks/status")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("MessageSid", TestData.PROVIDER_MESSAGE_ID)
                        .param("From", TwilioGateway.DEFAULT_FROM_PHONE.getNumber())
                        .param("MessageStatus", "delivered"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void whenTrustedPortAndSignatureVerified_ThenSucceeds() throws Exception {
        Message message = TestData.aMessage(TestData.aTemplateVariant().build()).build();
        SmsMessage smsMessage = TestData.anSmsMessage().message(message).build();
        Mockito.when(smsMessageRepository.findFirstByProviderMessageId(TestData.PROVIDER_MESSAGE_ID))
                .thenReturn(smsMessage);
        Mockito.when(twilioSignatureVerificationService.verifySignature(any())).thenReturn(true);

        mockMvc.perform(post("/public/twilio_callbacks/status")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("MessageSid", TestData.PROVIDER_MESSAGE_ID)
                        .param("From", TwilioGateway.DEFAULT_FROM_PHONE.getNumber())
                        .param("MessageStatus", "delivered"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = { "sending", "sent" })
    public void whenNewStatusIsSendingOrSent_ThenIgnore(String ignoredStatus) throws Exception {
        Message message = TestData.aMessage(TestData.aTemplateVariant().build()).build();
        SmsMessage smsMessage = TestData.anSmsMessage().message(message).build();
        Mockito.when(smsMessageRepository.findFirstByProviderMessageId(TestData.PROVIDER_MESSAGE_ID))
                .thenReturn(smsMessage);
        Mockito.when(twilioSignatureVerificationService.verifySignature(any())).thenReturn(true);

        mockMvc.perform(post("/public/twilio_callbacks/status")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("MessageSid", TestData.PROVIDER_MESSAGE_ID)
                        .param("From", TwilioGateway.DEFAULT_FROM_PHONE.getNumber())
                        .param("MessageStatus", ignoredStatus))
                .andExpect(MockMvcResultMatchers.status().isOk());
        assertNotEquals(ignoredStatus, message.getRawSmsStatus());
    }

    @Test
    public void whenNewStatusDelivered_ThenEnqueueStatusUpdateJobWithNoProviderError() throws Exception {
        String newStatus = "delivered";
        Message message = TestData.aMessage(TestData.aTemplateVariant().build()).build();
        SmsMessage smsMessage = TestData.anSmsMessage().message(message).build();
        Mockito.when(smsMessageRepository.findFirstByProviderMessageId(TestData.PROVIDER_MESSAGE_ID))
                .thenReturn(smsMessage);
        Mockito.when(twilioSignatureVerificationService.verifySignature(any())).thenReturn(true);

        mockMvc.perform(post("/public/twilio_callbacks/status")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("MessageSid", TestData.PROVIDER_MESSAGE_ID)
                        .param("From", TwilioGateway.DEFAULT_FROM_PHONE.getNumber())
                        .param("MessageStatus", newStatus))
                .andExpect(MockMvcResultMatchers.status().isOk());
        ArgumentCaptor<SmsMessageStatusUpdateJobRequest> smsMessageStatusUpdateJobRequestCaptor = ArgumentCaptor.forClass(SmsMessageStatusUpdateJobRequest.class);
        Mockito.verify(jobRequestScheduler).enqueue(smsMessageStatusUpdateJobRequestCaptor.capture());
        assertEquals(TestData.PROVIDER_MESSAGE_ID, smsMessageStatusUpdateJobRequestCaptor.getValue().getProviderMessageId());
        assertEquals(newStatus, smsMessageStatusUpdateJobRequestCaptor.getValue().getRawStatus());
        assertEquals(MessageStatus.delivered, smsMessageStatusUpdateJobRequestCaptor.getValue().getMessageStatus());
        assertNull(smsMessageStatusUpdateJobRequestCaptor.getValue().getProviderError());
    }

    @Test
    public void whenNewStatusUndelivered_ThenEnqueueStatusUpdateJobWithProviderError() throws Exception {
        String errorCode = "30005";
        String errorMessage = "Unknown destination handset";
        Message message = TestData.aMessage(TestData.aTemplateVariant().build()).build();
        SmsMessage smsMessage = TestData.anSmsMessage().message(message).build();
        Mockito.when(smsMessageRepository.findFirstByProviderMessageId(TestData.PROVIDER_MESSAGE_ID))
                .thenReturn(smsMessage);
        Mockito.when(twilioSignatureVerificationService.verifySignature(any())).thenReturn(true);

        mockMvc.perform(post("/public/twilio_callbacks/status")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("MessageSid", TestData.PROVIDER_MESSAGE_ID)
                        .param("From", TwilioGateway.DEFAULT_FROM_PHONE.getNumber())
                        .param("MessageStatus", "undelivered")
                        .param("ErrorCode", errorCode)
                        .param("ErrorMessage", errorMessage))
                .andExpect(MockMvcResultMatchers.status().isOk());
        ArgumentCaptor<SmsMessageStatusUpdateJobRequest> smsMessageStatusUpdateJobRequestCaptor = ArgumentCaptor.forClass(
                SmsMessageStatusUpdateJobRequest.class);
        Mockito.verify(jobRequestScheduler).enqueue(smsMessageStatusUpdateJobRequestCaptor.capture());
        assertEquals(TestData.PROVIDER_MESSAGE_ID, smsMessageStatusUpdateJobRequestCaptor.getValue().getProviderMessageId());
        assertEquals("undelivered", smsMessageStatusUpdateJobRequestCaptor.getValue().getRawStatus());
        assertEquals(MessageStatus.undelivered, smsMessageStatusUpdateJobRequestCaptor.getValue().getMessageStatus());
        assertEquals(Map.of("errorCode", "30005", "errorMessage", "Unknown destination handset"),
                smsMessageStatusUpdateJobRequestCaptor.getValue().getProviderError());
    }

    @Test
    public void whenInboundSTART_ThenSubscribes() throws Exception {
        Mockito.when(twilioSignatureVerificationService.verifySignature(any())).thenReturn(true);
        mockMvc.perform(post("/public/twilio_callbacks/inbound")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("From", TestData.TO_PHONE.getNumber())
                        .param("OptOutType", "START"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        Mockito.verify(smsService).subscribe(TestData.TO_PHONE);
    }

    @Test
    public void whenInboundSTOP_ThenUnsubscribes() throws Exception {
        Mockito.when(twilioSignatureVerificationService.verifySignature(any())).thenReturn(true);
        mockMvc.perform(post("/public/twilio_callbacks/inbound")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("From", TestData.TO_PHONE.getNumber())
                        .param("OptOutType", "STOP"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        Mockito.verify(smsService).unsubscribe(TestData.TO_PHONE);
    }

}
