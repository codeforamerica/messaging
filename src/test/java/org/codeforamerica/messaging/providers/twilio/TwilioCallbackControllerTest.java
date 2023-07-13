package org.codeforamerica.messaging.providers.twilio;

import org.codeforamerica.messaging.TestData;
import org.codeforamerica.messaging.config.SecurityConfiguration;
import org.codeforamerica.messaging.models.SmsMessage;
import org.codeforamerica.messaging.repositories.SmsMessageRepository;
import org.junit.jupiter.api.Test;
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
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(TwilioCallbackController.class)
@Import(SecurityConfiguration.class)
@TestPropertySource(properties = {"server.trustedPort=80"})
public class TwilioCallbackControllerTest {

    @MockBean
    SmsMessageRepository smsMessageRepository;
    @MockBean
    TwilioSignatureVerificationService twilioSignatureVerificationService;
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void whenTrustedPortAndSignatureVerified_ThenSucceeds() throws Exception {
        SmsMessage smsMessage = TestData.anSmsMessage().build();
        Mockito.when(smsMessageRepository.findFirstByProviderMessageId(TestData.PROVIDER_MESSAGE_ID))
                .thenReturn(smsMessage);
        Mockito.when(twilioSignatureVerificationService.verifySignature(any())).thenReturn(true);

        mockMvc.perform(post("/public/twilio_callbacks/status")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("MessageSid", TestData.PROVIDER_MESSAGE_ID)
                        .param("From", TwilioGateway.DEFAULT_FROM_PHONE)
                        .param("MessageStatus", "delivered"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        assertEquals("delivered", smsMessage.getStatus());
    }

    @Test
    public void whenUndelivered_ThenSavesProviderError() throws Exception {
        String errorCode = "30005";
        String errorMessage = "Unknown destination handset";
        SmsMessage smsMessage = TestData.anSmsMessage().build();
        Mockito.when(smsMessageRepository.findFirstByProviderMessageId(TestData.PROVIDER_MESSAGE_ID))
                .thenReturn(smsMessage);
        Mockito.when(twilioSignatureVerificationService.verifySignature(any())).thenReturn(true);

        mockMvc.perform(post("/public/twilio_callbacks/status")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("MessageSid", TestData.PROVIDER_MESSAGE_ID)
                        .param("From", TwilioGateway.DEFAULT_FROM_PHONE)
                        .param("MessageStatus", "undelivered")
                        .param("ErrorCode", errorCode)
                        .param("ErrorMessage", errorMessage))
                .andExpect(MockMvcResultMatchers.status().isOk());
        assertEquals(Map.of("errorCode", errorCode, "errorMessage", errorMessage), smsMessage.getProviderError());
    }

    @Test
    public void whenTrustedPortAndSignatureNotVerified_ThenUnauthorized() throws Exception {
        Mockito.when(smsMessageRepository.findFirstByProviderMessageId(TestData.PROVIDER_MESSAGE_ID))
                .thenReturn(TestData.anSmsMessage().build());
        Mockito.when(twilioSignatureVerificationService.verifySignature(any())).thenReturn(false);

        mockMvc.perform(post("/public/twilio_callbacks/status")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("MessageSid", TestData.PROVIDER_MESSAGE_ID)
                        .param("From", TwilioGateway.DEFAULT_FROM_PHONE)
                        .param("MessageStatus", "delivered"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

}
