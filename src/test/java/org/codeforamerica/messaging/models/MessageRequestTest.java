package org.codeforamerica.messaging.models;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.codeforamerica.messaging.TestData;
import org.codeforamerica.messaging.providers.twilio.TwilioGateway;
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
        MessageRequest messageRequest = MessageRequest.builder()
                .toPhone(candidate)
                .templateName(TestData.TEMPLATE_NAME)
                .build();
        Set<ConstraintViolation<MessageRequest>> violations = validator.validate(messageRequest);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = { "123456A7890", "123456789012" })
    public void rejectsInValidPhoneNumbers(String candidate) {
        MessageRequest messageRequest = MessageRequest.builder()
                .toPhone(candidate)
                .templateName(TestData.TEMPLATE_NAME)
                .build();
        Set<ConstraintViolation<MessageRequest>> violations = validator.validate(messageRequest);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void persistence() {
        Template template = TestData.aTemplate().build();
        TemplateVariant templateVariant = TestData.aDefaultTemplateVariant().build();
        template.addTemplateVariant(templateVariant);
        template = templateRepository.save(template);
        Message message = Message.builder()
                .toPhone(TestData.TO_PHONE)
                .toEmail(TestData.TO_EMAIL)
                .body(TestData.TEMPLATE_BODY_DEFAULT)
                .subject(TestData.TEMPLATE_SUBJECT_DEFAULT)
                .templateVariant(template.getTemplateVariants().get(0))
                .build();
        messageRepository.save(message);
        SmsMessage smsMessage = SmsMessage.builder()
                .toPhone(TestData.TO_PHONE)
                .fromPhone(TwilioGateway.DEFAULT_NUMBER)
                .body(TestData.TEMPLATE_BODY_DEFAULT)
                .status(TestData.STATUS)
                .providerMessageId(TestData.PROVIDER_MESSAGE_ID)
                .build();
        message.setSmsMessage(smsMessage);
        smsMessageRepository.save(smsMessage);
        messageRepository.save(message);
        messageRepository.delete(message);
        template.removeTemplateVariant(templateVariant);
        templateRepository.save(template);
        templateRepository.delete(template);
    }

}