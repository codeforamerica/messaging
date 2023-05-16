package org.codeforamerica.messaging.models;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.repositories.SmsMessageRepository;
import org.codeforamerica.messaging.repositories.TemplateRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
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
                .templateName("template_name")
                .build();
        Set<ConstraintViolation<MessageRequest>> violations = validator.validate(messageRequest);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = { "123456A7890", "123456789012" })
    public void rejectsInValidPhoneNumbers(String candidate) {
        MessageRequest messageRequest = MessageRequest.builder()
                .toPhone(candidate)
                .templateName("template_name")
                .build();
        Set<ConstraintViolation<MessageRequest>> violations = validator.validate(messageRequest);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void persistence() {
        String body = "some body";
        String toPhone = "1234567890";
        Template template = Template.builder()
                .name("test")
                .build();
        templateRepository.save(template);
        TemplateVariant templateVariant = TemplateVariant.builder()
                .body("body")
                .template(template)
                .build();
        template.setTemplateVariants(List.of(templateVariant));
        templateRepository.save(template);
        templateVariant = templateRepository.findFirstByNameIgnoreCase("test").getTemplateVariants().get(0);
        Message message = Message.builder()
                .toPhone(toPhone)
                .toEmail("sender@example.com")
                .body("some body")
                .subject("some subject")
                .templateVariant(templateVariant)
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