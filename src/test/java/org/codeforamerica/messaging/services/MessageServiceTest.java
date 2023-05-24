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
        template = templateRepository.save(template);
        template.addTemplateVariant(TemplateVariant.builder()
                .body(TestData.TEMPLATE_BODY_DEFAULT)
                .subject(TestData.TEMPLATE_SUBJECT_DEFAULT));
        template.addTemplateVariant(TemplateVariant.builder()
                .body(TestData.TEMPLATE_BODY_ES_B)
                .subject(TestData.TEMPLATE_SUBJECT_ES_B)
                .language("es")
                .treatment("B"));
        templateRepository.save(template);
    }

    @AfterEach
    void tearDown() {
        messageRepository.deleteAll();
        templateRepository.deleteAll();
    }

    @Test
    void whenOnlyPhone_thenOnlySmsServiceCalled() {
        Message message = messageService.saveMessage(TestData.aMessageRequest().toPhone(TestData.TO_PHONE).build());

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService).sendSmsMessage(TestData.TO_PHONE, TestData.TEMPLATE_BODY_DEFAULT);
        Mockito.verify(emailService, Mockito.never()).sendEmailMessage(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void whenOnlyEmail_thenOnlyEmailServiceCalled() {
        Message message = messageService.saveMessage(TestData.aMessageRequest().toEmail(TestData.TO_EMAIL).build());

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService, Mockito.never()).sendSmsMessage(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(emailService).sendEmailMessage(TestData.TO_EMAIL, TestData.TEMPLATE_BODY_DEFAULT, TestData.TEMPLATE_SUBJECT_DEFAULT);
    }

    @Test
    void whenBothPhoneAndEmail_thenBothServicesCalled() {
        Message message = messageService.saveMessage(TestData.aMessageRequest()
                .toPhone(TestData.TO_PHONE)
                .toEmail(TestData.TO_EMAIL)
                .build());

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService).sendSmsMessage(TestData.TO_PHONE, TestData.TEMPLATE_BODY_DEFAULT);
        Mockito.verify(emailService).sendEmailMessage(TestData.TO_EMAIL, TestData.TEMPLATE_BODY_DEFAULT, TestData.TEMPLATE_SUBJECT_DEFAULT);
    }

    @Test
    void whenScheduledWithBothPhoneAndEmail_thenBothServicesCalledAfterScheduleDelay() {
        MessageRequest messageRequest = TestData.aMessageRequest()
                .toPhone(TestData.TO_PHONE)
                .toEmail(TestData.TO_EMAIL)
                .sendAt(OffsetDateTime.now().plusSeconds(20))
                .build();

        SmsMessage smsMessage = TestData.anSmsMessage().build();
        smsMessage = smsMessageRepository.save(smsMessage);
        Mockito.when(smsService.sendSmsMessage(Mockito.any(), Mockito.any())).thenReturn(smsMessage);

        EmailMessage emailMessage = TestData.anEmailMessage().build();
        emailMessage = emailMessageRepository.save(emailMessage);
        Mockito.when(emailService.sendEmailMessage(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(emailMessage);

        Message message = messageService.scheduleMessage(messageRequest);
        await().atMost(60, SECONDS).until(() -> {
            var m = messageRepository.findById(message.getId()).get();
            return m.getSmsMessage() != null && m.getEmailMessage() != null;
        });
    }

    @Test
    void whenMessageRequestHasLanguageAndTreatment_thenValuesAreUsedToSelectTemplateVariant() {
        MessageRequest messageRequest = TestData.aMessageRequest()
                .toPhone(TestData.TO_PHONE)
                .toEmail(TestData.TO_EMAIL)
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
