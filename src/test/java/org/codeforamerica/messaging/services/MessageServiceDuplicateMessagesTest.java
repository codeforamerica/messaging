package org.codeforamerica.messaging.services;

import org.codeforamerica.messaging.TestData;
import org.codeforamerica.messaging.exceptions.MessageSendException;
import org.codeforamerica.messaging.models.*;
import org.codeforamerica.messaging.repositories.EmailMessageRepository;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.repositories.SmsMessageRepository;
import org.codeforamerica.messaging.repositories.TemplateRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Map;

import static org.codeforamerica.messaging.TestData.*;
import static org.codeforamerica.messaging.utils.CSVReader.LANGUAGE_HEADER;
import static org.codeforamerica.messaging.utils.CSVReader.TREATMENT_HEADER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;

@SpringBootTest(properties = {"message.duplicate-suppression-window-duration-in-hours=23"})
class MessageServiceDuplicateMessagesTest {
    @Autowired
    MessageService messageService;
    @MockBean
    SmsService smsService;
    @MockBean
    EmailService emailService;
    @Autowired
    MessageRepository messageRepository;
    @Autowired
    SmsMessageRepository smsMessageRepository;
    @Autowired
    EmailMessageRepository emailMessageRepository;
    @Autowired
    TemplateRepository templateRepository;

    Template template;

    @BeforeEach
    void setup() {
        template = TestData.aTemplate().build();
        template = templateRepository.save(template);
        template.addTemplateVariant(aTemplateVariant().build());
        template.addTemplateVariant(aTemplateVariant()
            .smsBody(TEMPLATE_BODY_EN_B)
            .emailBody(TEMPLATE_BODY_EN_B)
            .subject(TEMPLATE_SUBJECT_EN_B)
            .language("en")
            .treatment("B")
            .build());
        template.addTemplateVariant(aTemplateVariant()
            .smsBody(TEMPLATE_BODY_ES_A)
            .emailBody(TEMPLATE_BODY_ES_A)
            .subject(TEMPLATE_SUBJECT_ES_A)
            .language("es")
            .treatment("A")
            .build());
        template.addTemplateVariant(aTemplateVariant()
            .smsBody(TEMPLATE_BODY_ES_B)
            .emailBody(TEMPLATE_BODY_ES_B)
            .subject(TEMPLATE_SUBJECT_ES_B)
            .language("es")
            .treatment("B")
            .build());
        template = templateRepository.save(template);
    }

    @AfterEach
    void tearDown() {
        messageRepository.deleteAll();
        templateRepository.deleteAll();
    }

    @Test
    void whenMessageRequestIsDuplicateEmail_thenDoNotSendEmail() throws MessageSendException {
        EmailMessage sentEmailMessage = TestData.anEmailMessage()
            .build();
        emailMessageRepository.save(sentEmailMessage);
        Message sentMessage = TestData.aMessage(template.getTemplateVariant("en", "A").get())
            .emailMessage(sentEmailMessage)
            .emailStatus(MessageStatus.delivered)
            .build();
        messageRepository.save(sentMessage);

        MessageRequest messageRequest = TestData.aMessageRequest()
            .toEmail(TestData.TO_EMAIL)
            .build();
        Message message = messageService.saveMessage(messageRequest, null);

        messageService.sendMessage(message.getId());
        Mockito.verify(emailService, never()).sendEmailMessage(anyString(), anyString(), anyString());
        Assertions.assertEquals(MessageStatus.duplicate, messageRepository.findById(message.getId()).get().getEmailStatus());
        Assertions.assertEquals("Duplicate message", messageRepository.findById(message.getId()).get().getEmailErrorMessage());
    }

    @Test
    void whenMessageRequestIsDuplicateEmailNewTreatment_thenDoNotSendEmail() throws MessageSendException {
        EmailMessage sentEmailMessage = TestData.anEmailMessage()
            .build();
        emailMessageRepository.save(sentEmailMessage);
        Message sentMessage = TestData.aMessage(template.getTemplateVariant("en", "A").get())
            .emailMessage(sentEmailMessage)
            .emailStatus(MessageStatus.delivered)
            .build();
        messageRepository.save(sentMessage);

        MessageRequest messageRequest = TestData.aMessageRequest()
            .toEmail(TestData.TO_EMAIL)
            .templateParams(Map.of(
                "placeholder", "{{{placeholder}}}",
                TREATMENT_HEADER, "B"))
            .build();
        Message message = messageService.saveMessage(messageRequest, null);

        messageService.sendMessage(message.getId());
        Mockito.verify(emailService, never()).sendEmailMessage(anyString(), anyString(), anyString());
        Assertions.assertEquals(MessageStatus.duplicate, messageRepository.findById(message.getId()).get().getEmailStatus());
        Assertions.assertEquals("Duplicate message", messageRepository.findById(message.getId()).get().getEmailErrorMessage());
    }

    @Test
    void whenMessageRequestIsDuplicateEmailNewTreatmentNewPlaceholder_thenSendEmail() throws MessageSendException {
        EmailMessage sentEmailMessage = TestData.anEmailMessage()
            .build();
        emailMessageRepository.save(sentEmailMessage);
        Message sentMessage = TestData.aMessage(template.getTemplateVariant("en", "A").get())
            .emailMessage(sentEmailMessage)
            .emailStatus(MessageStatus.delivered)
            .build();
        messageRepository.save(sentMessage);

        MessageRequest messageRequest = TestData.aMessageRequest()
            .toEmail(TestData.TO_EMAIL)
            .templateParams(Map.of(
                "placeholder", "new placeholder",
                TREATMENT_HEADER, "B"))
            .build();
        Message message = messageService.saveMessage(messageRequest, null);

        messageService.sendMessage(message.getId());
        Mockito.verify(emailService).sendEmailMessage(anyString(), anyString(), anyString());
    }

    @Test
    void whenMessageRequestIsSameTemplateSamePlaceholdersNewLanguage_thenSendEmail() throws MessageSendException {
        EmailMessage sentEmailMessage = TestData.anEmailMessage()
            .build();
        emailMessageRepository.save(sentEmailMessage);
        Message sentMessage = TestData.aMessage(template.getTemplateVariant("en", "A").get())
            .emailMessage(sentEmailMessage)
            .emailStatus(MessageStatus.delivered)
            .build();
        messageRepository.save(sentMessage);

        MessageRequest messageRequest = TestData.aMessageRequest()
            .toEmail(TestData.TO_EMAIL)
            .templateParams(Map.of(
                "placeholder", "{{{placeholder}}}",
                LANGUAGE_HEADER, "es"))
            .build();
        Message message = messageService.saveMessage(messageRequest, null);

        messageService.sendMessage(message.getId());
        Mockito.verify(emailService).sendEmailMessage(anyString(), anyString(), anyString());
    }

    @Test
    void whenMessageRequestIsDuplicateSms_thenDoNotSendSms() throws MessageSendException {
        SmsMessage sentSmsMessage = TestData.anSmsMessage()
            .build();
        smsMessageRepository.save(sentSmsMessage);
        Message sentMessage = TestData.aMessage(template.getTemplateVariant("en", "A").get())
            .smsMessage(sentSmsMessage)
            .smsStatus(MessageStatus.delivered)
            .build();
        messageRepository.save(sentMessage);

        MessageRequest messageRequest = TestData.aMessageRequest()
            .toPhone(TestData.TO_PHONE)
            .build();
        Message message = messageService.saveMessage(messageRequest, null);

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService, never()).sendSmsMessage(any(), anyString());
        Assertions.assertEquals(MessageStatus.duplicate, messageRepository.findById(message.getId()).get().getSmsStatus());
        Assertions.assertEquals("Duplicate message", messageRepository.findById(message.getId()).get().getSmsErrorMessage());
    }

    @Test
    void whenMessageRequestIsDuplicateSmsNewTreatment_thenDoNotSendSms() throws MessageSendException {
        SmsMessage sentSmsMessage = TestData.anSmsMessage()
            .build();
        smsMessageRepository.save(sentSmsMessage);
        Message sentMessage = TestData.aMessage(template.getTemplateVariant("en", "A").get())
            .smsMessage(sentSmsMessage)
            .smsStatus(MessageStatus.delivered)
            .build();
        messageRepository.save(sentMessage);

        MessageRequest messageRequest = TestData.aMessageRequest()
            .toPhone(TestData.TO_PHONE)
            .templateParams(Map.of(
                "placeholder", "{{{placeholder}}}",
                TREATMENT_HEADER, "B"))
            .build();
        Message message = messageService.saveMessage(messageRequest, null);

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService, never()).sendSmsMessage(any(), anyString());
        Assertions.assertEquals(MessageStatus.duplicate, messageRepository.findById(message.getId()).get().getSmsStatus());
        Assertions.assertEquals("Duplicate message", messageRepository.findById(message.getId()).get().getSmsErrorMessage());
    }

    @Test
    void whenMessageRequestIsDuplicateSmsNewTreatmentNewPlaceholder_thenSendSms() throws MessageSendException {
        SmsMessage sentSmsMessage = TestData.anSmsMessage()
            .build();
        smsMessageRepository.save(sentSmsMessage);
        Message sentMessage = TestData.aMessage(template.getTemplateVariant("en", "A").get())
            .smsMessage(sentSmsMessage)
            .smsStatus(MessageStatus.delivered)
            .build();
        messageRepository.save(sentMessage);

        MessageRequest messageRequest = TestData.aMessageRequest()
            .toPhone(TestData.TO_PHONE)
            .templateParams(Map.of(
                "placeholder", "new placeholder",
                TREATMENT_HEADER, "B"))
            .build();
        Message message = messageService.saveMessage(messageRequest, null);

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService).sendSmsMessage(any(), anyString());
    }

    @Test
    void whenMessageRequestIsSameTemplateSamePlaceholdersNewLanguage_thenSendSms() throws MessageSendException {
        SmsMessage sentSmsMessage = TestData.anSmsMessage()
            .build();
        smsMessageRepository.save(sentSmsMessage);
        Message sentMessage = TestData.aMessage(template.getTemplateVariant("en", "A").get())
            .smsMessage(sentSmsMessage)
            .smsStatus(MessageStatus.delivered)
            .build();
        messageRepository.save(sentMessage);

        MessageRequest messageRequest = TestData.aMessageRequest()
            .toPhone(TestData.TO_PHONE)
            .templateParams(Map.of(
                "placeholder", "{{{placeholder}}}",
                LANGUAGE_HEADER, "es"))
            .build();
        Message message = messageService.saveMessage(messageRequest, null);

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService).sendSmsMessage(any(), anyString());
    }
}
