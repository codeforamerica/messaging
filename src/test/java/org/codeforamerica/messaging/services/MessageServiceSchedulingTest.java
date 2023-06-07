package org.codeforamerica.messaging.services;

import org.codeforamerica.messaging.TestData;
import org.codeforamerica.messaging.models.*;
import org.codeforamerica.messaging.repositories.EmailMessageRepository;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.repositories.SmsMessageRepository;
import org.codeforamerica.messaging.repositories.TemplateRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.OffsetDateTime;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

@SpringBootTest
public class MessageServiceSchedulingTest {
    public Template template;

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
    @Disabled
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
}
