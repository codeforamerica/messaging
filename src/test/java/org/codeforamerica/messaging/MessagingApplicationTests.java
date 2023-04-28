package org.codeforamerica.messaging;

import org.assertj.core.api.Assertions;
import org.codeforamerica.messaging.controllers.MessageController;
import org.codeforamerica.messaging.providers.mailgun.MailgunCallbackController;
import org.codeforamerica.messaging.providers.twilio.TwilioCallbackController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MessagingApplicationTests {

    @Autowired
    private MessageController messageController;
    @Autowired
    private TwilioCallbackController twilioCallbackController;
    @Autowired
    private MailgunCallbackController mailgunCallbackController;

    @Test
    public void contextLoads() {
        Assertions.assertThat(messageController).isNotNull();
        Assertions.assertThat(twilioCallbackController).isNotNull();
        Assertions.assertThat(mailgunCallbackController).isNotNull();
    }
}
