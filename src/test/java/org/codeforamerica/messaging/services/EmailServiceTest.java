package org.codeforamerica.messaging.services;

import org.codeforamerica.messaging.exceptions.MessageSendException;
import org.codeforamerica.messaging.exceptions.UnsubscribedException;
import org.codeforamerica.messaging.models.EmailSubscription;
import org.codeforamerica.messaging.providers.mailgun.MailgunGateway;
import org.codeforamerica.messaging.repositories.EmailMessageRepository;
import org.codeforamerica.messaging.repositories.EmailSubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
class EmailServiceTest {
    @MockBean
    EmailSubscriptionRepository emailSubscriptionRepository;
    @MockBean
    MailgunGateway mailgunGateway;
    @Autowired
    EmailService emailService;
    @MockBean
    EmailMessageRepository emailMessageRepository;

    @Test
    public void whenNoLatestEmailSubscription_ThenSendsEmail() throws MessageSendException {
        Mockito.when(emailSubscriptionRepository.findFirstByEmailOrderByCreationTimestampDesc(any()))
                .thenReturn(null);
        emailService.sendEmailMessage("subscribed@example.com", "some body", "some subject");
        Mockito.verify(mailgunGateway).sendMessage(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void whenLatestEmailSubscriptionIsSubscribed_ThenSendsEmail() throws MessageSendException {
        Mockito.when(emailSubscriptionRepository.findFirstByEmailOrderByCreationTimestampDesc(any()))
                .thenReturn(EmailSubscription.builder().unsubscribed(false).build());
        emailService.sendEmailMessage("subscribed@example.com", "some body", "some subject");
        Mockito.verify(mailgunGateway).sendMessage(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void whenLatestEmailSubscriptionIsUnsubscribed_ThenThrowsUnsubscribedException() {
        Mockito.when(emailSubscriptionRepository.findFirstByEmailOrderByCreationTimestampDesc(any()))
                .thenReturn(EmailSubscription.builder().unsubscribed(true).build());
        assertThrowsExactly(UnsubscribedException.class, () ->  emailService.sendEmailMessage("unsubscribed@example.com", "some body", "some subject"));
    }

}
