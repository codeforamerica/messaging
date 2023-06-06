package org.codeforamerica.messaging.models;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.codeforamerica.messaging.TestData;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.repositories.SmsMessageRepository;
import org.codeforamerica.messaging.repositories.TemplateRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
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
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private SmsMessageRepository smsMessageRepository;
    @Autowired
    private TemplateRepository templateRepository;

    @AfterEach
    void tearDown() {
        templateRepository.deleteAll();
        messageRepository.deleteAll();
    }

    @ParameterizedTest
    @ValueSource(strings = { "1234567890", "11234567890", "+11234567890" })
    public void acceptsValidPhoneNumbers(String candidate) {
        Set<ConstraintViolation<MessageRequest>> violations = validator.validate(
                TestData.aMessageRequest()
                        .toPhone(candidate)
                        .build());
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = { "123456A7890", "123456789012" })
    public void rejectsInValidPhoneNumbers(String candidate) {
        Set<ConstraintViolation<MessageRequest>> violations = validator.validate(
                TestData.aMessageRequest()
                        .toPhone(candidate)
                        .build());
        assertFalse(violations.isEmpty());
    }

    @Test
    public void persistence() {
        Template template = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template);
        template = templateRepository.save(template);
        Message message = TestData.aMessage(template.getTemplateVariants().stream().findFirst().get())
                .toPhone(TestData.TO_PHONE)
                .toEmail(TestData.TO_EMAIL)
                .build();
        message = messageRepository.save(message);
        SmsMessage smsMessage = TestData.anSmsMessage().build();
        message.setSmsMessage(smsMessage);
        smsMessageRepository.save(smsMessage);

        message = messageRepository.save(message);
        messageRepository.delete(message);
    }

}