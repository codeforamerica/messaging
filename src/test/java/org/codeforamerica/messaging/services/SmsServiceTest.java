package org.codeforamerica.messaging.services;

import org.codeforamerica.messaging.models.Message;
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

        Message message = Message.builder()
                .to(to)
                .from("from")
                .body(body)
                .status("fixme")
                .providerMessageId(providerMessageId)
                .createdAt(OffsetDateTime.now())
                .build();

        Mockito.when(twilioGateway.sendMessage(to, body)).thenReturn(message);
        Mockito.when(messageRepository.save(message)).thenReturn(message);

        message = smsService.sendSmsMessage(to, body);

        Assertions.assertEquals(providerMessageId, message.getProviderMessageId());
        Assertions.assertEquals(to, message.getTo());
        Assertions.assertEquals(body, message.getBody());
    }

}
