package org.codeforamerica.messaging.providers.twilio;

import org.codeforamerica.messaging.TestData;
import org.codeforamerica.messaging.config.SecurityConfiguration;
import org.codeforamerica.messaging.repositories.SmsMessageRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(TwilioCallbackController.class)
@Import(SecurityConfiguration.class)
public class TwilioCallbackControllerTest {

    @MockBean
    SmsMessageRepository smsMessageRepository;
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void postSmsStatusSuccessUnauthenticated() throws Exception {
        Mockito.when(smsMessageRepository.findFirstByProviderMessageId(any()))
                .thenReturn(TestData.anSmsMessage().build());

        mockMvc.perform(post("/gateway/twilio_callbacks/status")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("MessageSid", TestData.PROVIDER_MESSAGE_ID)
                        .param("From", TwilioGateway.DEFAULT_FROM_PHONE)
                        .param("MessageStatus", "delivered"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

}
