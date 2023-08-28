package org.codeforamerica.messaging.services;

import org.codeforamerica.messaging.exceptions.MessageSendException;
import org.codeforamerica.messaging.exceptions.UnsubscribedException;
import org.codeforamerica.messaging.models.PhoneNumber;
import org.codeforamerica.messaging.models.SmsSubscription;
import org.codeforamerica.messaging.providers.twilio.TwilioGateway;
import org.codeforamerica.messaging.repositories.SmsMessageRepository;
import org.codeforamerica.messaging.repositories.SmsSubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
class SmsServiceTest {
    @MockBean
    SmsSubscriptionRepository smsSubscriptionRepository;
    @MockBean
    TwilioGateway twilioGateway;
    @Autowired
    SmsService smsService;
    @MockBean
    SmsMessageRepository smsMessageRepository;

    @Test
    public void whenNoLatestSmsSubscription_ThenSendsSms() throws MessageSendException {
        Mockito.when(smsSubscriptionRepository.findFirstByPhoneNumberOrderByCreationTimestampDesc(any()))
                .thenReturn(null);
        smsService.sendSmsMessage(PhoneNumber.valueOf("8005551212"), "some body");
        Mockito.verify(twilioGateway).sendMessage(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void whenLatestSmsSubscriptionIsSubscribed_ThenSendsSmsl() throws MessageSendException {
        Mockito.when(smsSubscriptionRepository.findFirstByPhoneNumberOrderByCreationTimestampDesc(any()))
                .thenReturn(SmsSubscription.builder().unsubscribed(false).build());
        smsService.sendSmsMessage(PhoneNumber.valueOf("8005551212"), "some body");
        Mockito.verify(twilioGateway).sendMessage(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void whenLatestSmsSubscriptionIsUnsubscribed_ThenThrowsUnsubscribedException() {
        Mockito.when(smsSubscriptionRepository.findFirstByPhoneNumberOrderByCreationTimestampDesc(any()))
                .thenReturn(SmsSubscription.builder().unsubscribed(true).build());
        assertThrowsExactly(UnsubscribedException.class, () ->  smsService.sendSmsMessage(PhoneNumber.valueOf("8005551212"), "some body"));
    }
}
