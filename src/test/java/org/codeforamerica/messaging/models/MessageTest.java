package org.codeforamerica.messaging.models;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.repositories.SmsMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class MessageTest {

    @Autowired
    private Validator validator;

    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private SmsMessageRepository smsMessageRepository;

    @ParameterizedTest
    @ValueSource(strings = { "1234567890", "11234567890", "+11234567890" })
    public void acceptsValidPhoneNumbers(String candidate) {
        Message mr = Message.builder()
                .toPhone(candidate)
                .toEmail("sender@example.com")
                .body("some body")
                .build();
        Set<ConstraintViolation<Message>> violations = validator.validate(mr);
        assertTrue(violations.isEmpty());
    }


    @ParameterizedTest
    @ValueSource(strings = { "123456A7890", "123456789012" })
    public void rejectsInValidPhoneNumbers(String candidate) {
        Message mr = Message.builder()
                .toPhone(candidate)
                .toEmail("sender@example.com")
                .body("some body")
                .build();
        Set<ConstraintViolation<Message>> violations = validator.validate(mr);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void persistence() {
        String body = "some body";
        String toPhone = "1234567890";
        Message message = Message.builder()
                .toPhone(toPhone)
                .toEmail("sender@example.com")
                .body("some body")
                .build();
        messageRepository.save(message);
        SmsMessage smsMessage = SmsMessage.builder()
                .toPhone(toPhone)
                .fromPhone("0000000000")
                .body(body)
                .status("fixme")
                .providerMessageId("some-provider-message-id")
                .build();
        message.setSmsMessage(smsMessage);
        smsMessageRepository.save(smsMessage);
        messageRepository.save(message);
        messageRepository.delete(message);

    }

}