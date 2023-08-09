package org.codeforamerica.messaging.services;

import org.codeforamerica.messaging.TestData;
import org.codeforamerica.messaging.exceptions.MessageSendException;
import org.codeforamerica.messaging.models.Message;
import org.codeforamerica.messaging.models.Template;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.repositories.TemplateRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;


@SpringBootTest
public class MessageServiceErrorHandlingTest {
    public Template template;

    @MockBean
    SmsService smsService;

    @MockBean
    EmailService emailService;

    @Autowired
    MessageService messageService;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    TemplateRepository templateRepository;

    @BeforeEach
    void setup() throws Exception {
        template = TestData.aTemplate().build();
        template = templateRepository.save(template);
        TestData.addVariantsToTemplate(template);
        templateRepository.save(template);
    }

    @AfterEach
    void tearDown() {
        messageRepository.deleteAll();
        templateRepository.deleteAll();
    }


    @Test
    void whenSmsServiceThrowsMessageSendException_ThenExceptionErrorMessageIsSaved() throws MessageSendException {
        Mockito.when(smsService.sendSmsMessage(any(), any()))
                .thenThrow(new MessageSendException("Houston, we have a problem"));
        Message message = messageService.saveMessage(TestData.aMessageRequest().toPhone(TestData.TO_PHONE).templateName(template.getName()).build(), null);

        messageService.sendMessage(message.getId());
        Message attemptedMessage = messageRepository.findById(message.getId()).get();
        assertEquals("submission_failed", attemptedMessage.getSmsStatus());
        assertEquals("Houston, we have a problem", attemptedMessage.getSmsErrorMessage());
    }

    @Test
    void whenEmailServiceThrowsMessageSendException_ThenExceptionErrorMessageIsSaved() throws MessageSendException {
        Mockito.when(emailService.sendEmailMessage(any(), any(), any()))
                .thenThrow(new MessageSendException("Houston, we have a problem"));
        Message message = messageService.saveMessage(TestData.aMessageRequest().toEmail(TestData.TO_EMAIL).templateName(template.getName()).build(), null);

        messageService.sendMessage(message.getId());
        Message attemptedMessage = messageRepository.findById(message.getId()).get();
        assertEquals("submission_failed", attemptedMessage.getEmailStatus());
        assertEquals("Houston, we have a problem", attemptedMessage.getEmailErrorMessage());
    }

}
