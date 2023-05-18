package org.codeforamerica.messaging.services;

import org.codeforamerica.messaging.TestData;
import org.codeforamerica.messaging.models.*;
import org.codeforamerica.messaging.repositories.EmailMessageRepository;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.repositories.SmsMessageRepository;
import org.codeforamerica.messaging.repositories.TemplateRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.OffsetDateTime;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

@SpringBootTest
class MessageServiceTest {

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

    @BeforeEach
    void setup() {
        Template template = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template);
        templateRepository.save(template);
    }

    @AfterEach
    void tearDown() {
        messageRepository.deleteAll();
        templateRepository.deleteAll();
    }

    @Test
    void whenOnlyPhone_thenOnlySmsServiceCalled() {
        MessageRequest messageRequest = MessageRequest.builder()
                .toPhone(TestData.TO_PHONE)
                .templateName(TestData.TEMPLATE_NAME)
                .templateParams(Map.of("placeholder", "{{placeholder}}"))
                .build();
        Message message = messageService.saveMessage(messageRequest);

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService).sendSmsMessage(messageRequest.getToPhone(), TestData.TEMPLATE_BODY_DEFAULT);
        Mockito.verify(emailService, Mockito.never()).sendEmailMessage(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void whenOnlyEmail_thenOnlyEmailServiceCalled() {
        MessageRequest messageRequest = MessageRequest.builder()
                .toEmail(TestData.TO_EMAIL)
                .templateName(TestData.TEMPLATE_NAME)
                .templateParams(Map.of("placeholder", "{{placeholder}}"))
                .build();
        Message message = messageService.saveMessage(messageRequest);

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService, Mockito.never()).sendSmsMessage(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(emailService).sendEmailMessage(message.getToEmail(), TestData.TEMPLATE_BODY_DEFAULT, TestData.TEMPLATE_SUBJECT_DEFAULT);
    }

    @Test
    void whenBothPhoneAndEmail_thenBothServicesCalled() {
        MessageRequest messageRequest = MessageRequest.builder()
                .toPhone(TestData.TO_PHONE)
                .toEmail(TestData.TO_EMAIL)
                .templateName(TestData.TEMPLATE_NAME)
                .templateParams(Map.of("placeholder", "{{placeholder}}"))
                .build();
        Message message = messageService.saveMessage(messageRequest);

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService).sendSmsMessage(messageRequest.getToPhone(), TestData.TEMPLATE_BODY_DEFAULT);
        Mockito.verify(emailService).sendEmailMessage(message.getToEmail(), TestData.TEMPLATE_BODY_DEFAULT, TestData.TEMPLATE_SUBJECT_DEFAULT);
    }

    @Test
    void whenScheduledWithBothPhoneAndEmail_thenBothServicesCalledAfterScheduleDelay() {
        MessageRequest messageRequest = MessageRequest.builder()
                .toPhone(TestData.TO_PHONE)
                .toEmail(TestData.TO_EMAIL)
                .templateName(TestData.TEMPLATE_NAME)
                .templateParams(Map.of("placeholder", "{{placeholder}}"))
                .sendAt(OffsetDateTime.now().plusSeconds(20))
                .build();
        SmsMessage smsMessage = SmsMessage.builder()
                .body(TestData.TEMPLATE_BODY_DEFAULT)
                .toPhone(messageRequest.getToPhone())
                .fromPhone(TestData.TO_PHONE)
                .status(TestData.STATUS)
                .providerMessageId(TestData.PROVIDER_MESSAGE_ID)
                .build();
        smsMessageRepository.save(smsMessage);

        Mockito.when(smsService.sendSmsMessage(Mockito.any(), Mockito.any())).thenReturn(smsMessage);

        EmailMessage emailMessage = EmailMessage.builder()
                .body(TestData.TEMPLATE_BODY_DEFAULT)
                .subject(TestData.TEMPLATE_SUBJECT_DEFAULT)
                .toEmail(messageRequest.getToEmail())
                .fromEmail(TestData.TO_EMAIL)
                .status(TestData.STATUS)
                .providerMessageId(TestData.PROVIDER_MESSAGE_ID)
                .build();
        Mockito.when(emailService.sendEmailMessage(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(emailMessage);
        emailMessageRepository.save(emailMessage);

        Message message = messageService.scheduleMessage(messageRequest);
        await().atMost(60, SECONDS).until(() -> {
            var m = messageRepository.findById(message.getId()).get();
            return m.getSmsMessage() != null && m.getEmailMessage() != null;
        });
    }

    @Test
    void whenMessageRequestHasLanguageAndTreatment_thenValuesAreUsedToSelectTemplateVariant() {
        MessageRequest messageRequest = MessageRequest.builder()
                .toPhone(TestData.TO_PHONE)
                .toEmail(TestData.TO_EMAIL)
                .templateName(TestData.TEMPLATE_NAME)
                .templateParams(Map.of(
                        "language", "es",
                        "treatment", "B",
                        "placeholder", "{{placeholder}}"))
                .build();
        Message message = messageService.saveMessage(messageRequest);

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService).sendSmsMessage(messageRequest.getToPhone(), TestData.TEMPLATE_BODY_ES_B);
        Mockito.verify(emailService).sendEmailMessage(message.getToEmail(), TestData.TEMPLATE_BODY_ES_B, TestData.TEMPLATE_SUBJECT_ES_B);
    }

}
