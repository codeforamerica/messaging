package org.codeforamerica.messaging.services;

import org.codeforamerica.messaging.models.SmsMessage;
import org.codeforamerica.messaging.providers.twilio.TwilioGateway;
import org.codeforamerica.messaging.repositories.SmsMessageRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.OffsetDateTime;


@SpringBootTest
public class SmsServiceTest {

    @MockBean
    TwilioGateway twilioGateway;
    @MockBean
    SmsMessageRepository smsMessageRepository;
    @Autowired
    SmsService smsService;

    @AfterEach
    void teardown() {
        smsMessageRepository.deleteAll();
    }

    @Test
    void sendSmsMessage() {
        String to = "8005551212";
        String body = "Hello";
        String providerMessageId = "ABCD";

        SmsMessage smsMessage = SmsMessage.builder()
                .toPhone(to)
                .fromPhone("from")
                .body(body)
                .status("fixme")
                .providerMessageId(providerMessageId)
                .createdAt(OffsetDateTime.now())
                .build();

        Mockito.when(twilioGateway.sendMessage(to, body)).thenReturn(smsMessage);
        Mockito.when(smsMessageRepository.save(smsMessage)).thenReturn(smsMessage);

        smsMessage = smsService.sendSmsMessage(to, body);

        Assertions.assertEquals(providerMessageId, smsMessage.getProviderMessageId());
        Assertions.assertEquals(to, smsMessage.getToPhone());
        Assertions.assertEquals(body, smsMessage.getBody());
    }

}
