package org.codeforamerica.messaging.models;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class MessageRequestTest {

    @Autowired
    private Validator validator;

    @ParameterizedTest
    @ValueSource(strings = { "1234567890", "11234567890", "+11234567890" })
    public void acceptsValidPhoneNumbers(String candidate) {
        MessageRequest mr = MessageRequest.builder()
                .toPhone(candidate)
                .toEmail("sender@example.com")
                .body("some body")
                .build();
        Set<ConstraintViolation<MessageRequest>> violations = validator.validate(mr);
        assertTrue(violations.isEmpty());
    }


    @ParameterizedTest
    @ValueSource(strings = { "123456A7890", "123456789012" })
    public void rejectsInValidPhoneNumbers(String candidate) {
        MessageRequest mr = MessageRequest.builder()
                .toPhone(candidate)
                .toEmail("sender@example.com")
                .body("some body")
                .build();
        Set<ConstraintViolation<MessageRequest>> violations = validator.validate(mr);
        assertFalse(violations.isEmpty());
    }

}