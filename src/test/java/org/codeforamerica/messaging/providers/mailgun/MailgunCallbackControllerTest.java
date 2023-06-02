package org.codeforamerica.messaging.providers.mailgun;

import org.codeforamerica.messaging.TestData;
import org.codeforamerica.messaging.config.SecurityConfiguration;
import org.codeforamerica.messaging.providers.mailgun.MailgunCallbackController;
import org.codeforamerica.messaging.repositories.EmailMessageRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(MailgunCallbackController.class)
@Import(SecurityConfiguration.class)
public class MailgunCallbackControllerTest {

    @MockBean
    EmailMessageRepository emailMessageRepository;
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void postEmailStatusSuccess() throws Exception {
        Mockito.when(emailMessageRepository.findFirstByProviderMessageId(TestData.PROVIDER_MESSAGE_ID))
                .thenReturn(TestData.anEmailMessage().build());

        mockMvc.perform(post("/mailgun_callbacks/status")
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

}
