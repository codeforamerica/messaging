package org.codeforamerica.messaging.providers.twilio;

import org.codeforamerica.messaging.config.SecurityConfiguration;
import org.codeforamerica.messaging.models.SmsMessage;
import org.codeforamerica.messaging.repositories.MessageRepository;
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
    MessageRepository messageRepository;
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void postSmsStatusSuccessUnauthenticated() throws Exception {
        Mockito.when(messageRepository.findFirstByProviderMessageId(any()))
                .thenReturn(SmsMessage.builder().providerMessageId("message_id").build());

        mockMvc.perform(post("/twilio_callbacks/status")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .param("MessageSid", "SM13234")
                        .param("From", "1234567890")
                        .param("MessageStatus", "delivered"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

}
