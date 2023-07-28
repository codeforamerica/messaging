package org.codeforamerica.messaging.services;

import org.codeforamerica.messaging.models.EmailSubscription;
import org.codeforamerica.messaging.providers.mailgun.MailgunGateway;
import org.codeforamerica.messaging.repositories.EmailMessageRepository;
import org.codeforamerica.messaging.repositories.EmailSubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

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
    public void whenNoLatestEmailSubscription_ThenSendsEmail() {
        Mockito.when(emailSubscriptionRepository.findFirstByEmailOrderByCreationTimestampDesc(any()))
                .thenReturn(null);
        emailService.sendEmailMessage("subscribed@exmaple.com", "some body", "some subject");
        Mockito.verify(mailgunGateway).sendMessage(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void whenLatestEmailSubscriptionIsSubscribed_ThenSendsEmail() {
        Mockito.when(emailSubscriptionRepository.findFirstByEmailOrderByCreationTimestampDesc(any()))
                .thenReturn(EmailSubscription.builder().unsubscribed(false).build());
        emailService.sendEmailMessage("subscribed@exmaple.com", "some body", "some subject");
        Mockito.verify(mailgunGateway).sendMessage(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void whenLatestEmailSubscriptionIsUnsubscribed_ThenDoesNotSendEmail() {
        Mockito.when(emailSubscriptionRepository.findFirstByEmailOrderByCreationTimestampDesc(any()))
                .thenReturn(EmailSubscription.builder().unsubscribed(true).build());
        emailService.sendEmailMessage("subscribed@exmaple.com", "some body", "some subject");
        Mockito.verify(mailgunGateway, Mockito.never()).sendMessage("subscribed@exmaple.com", "some body", "some subject");
    }

}
