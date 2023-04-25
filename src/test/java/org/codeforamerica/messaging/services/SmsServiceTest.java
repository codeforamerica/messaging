package org.codeforamerica.messaging.services;

import org.codeforamerica.messaging.models.SmsMessage;
import org.codeforamerica.messaging.providers.twilio.TwilioGateway;
import org.codeforamerica.messaging.repositories.MessageRepository;
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
    MessageRepository messageRepository;
    @Autowired
    SmsService smsService;

    @AfterEach
    void teardown() {
        messageRepository.deleteAll();
    }

    @Test
    void sendSmsMessage() {
        String to = "8005551212";
        String body = "Hello";
        String providerMessageId = "ABCD";

        SmsMessage smsMessage = SmsMessage.builder()
                .toNumber(to)
                .fromNumber("from")
                .body(body)
                .status("fixme")
                .providerMessageId(providerMessageId)
                .createdAt(OffsetDateTime.now())
                .build();

        Mockito.when(twilioGateway.sendMessage(to, body)).thenReturn(smsMessage);
        Mockito.when(messageRepository.save(smsMessage)).thenReturn(smsMessage);

        smsMessage = smsService.sendSmsMessage(to, body);

        Assertions.assertEquals(providerMessageId, smsMessage.getProviderMessageId());
        Assertions.assertEquals(to, smsMessage.getToNumber());
        Assertions.assertEquals(body, smsMessage.getBody());
    }

}
