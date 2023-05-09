package org.codeforamerica.messaging.services;

import org.codeforamerica.messaging.models.EmailMessage;
import org.codeforamerica.messaging.models.Message;
import org.codeforamerica.messaging.models.MessageRequest;
import org.codeforamerica.messaging.models.SmsMessage;
import org.codeforamerica.messaging.repositories.EmailMessageRepository;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.repositories.SmsMessageRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.OffsetDateTime;

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


    @AfterEach
    void tearDown() {
        messageRepository.deleteAll();
    }

    @Test
    void scheduleMessage() {
    }

    @Test
    void whenOnlyPhone_thenOnlySmsServiceCalled() {
        MessageRequest messageRequest = MessageRequest.builder()
                .toPhone("8005551212")
                .body("Test message")
                .build();
        Message message = messageService.saveMessage(messageRequest);

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService).sendSmsMessage(messageRequest.getToPhone(), messageRequest.getBody());
        Mockito.verify(emailService, Mockito.never()).sendEmailMessage(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void whenOnlyEmail_thenOnlyEmailServiceCalled() {
        MessageRequest messageRequest = MessageRequest.builder()
                .toEmail("foo@example.com")
                .subject("Test subject")
                .body("Test message")
                .build();
        Message message = messageService.saveMessage(messageRequest);

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService, Mockito.never()).sendSmsMessage(Mockito.any(), Mockito.any());
        Mockito.verify(emailService).sendEmailMessage(messageRequest.getToEmail(), messageRequest.getBody(), messageRequest.getSubject());
    }

    @Test
    void whenBothPhoneAndEmail_thenBothServicesCalled() {
        MessageRequest messageRequest = MessageRequest.builder()
                .toPhone("8005551212")
                .toEmail("foo@example.com")
                .subject("Test subject")
                .body("Test message")
                .build();
        Message message = messageService.saveMessage(messageRequest);

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService).sendSmsMessage(messageRequest.getToPhone(), messageRequest.getBody());
        Mockito.verify(emailService).sendEmailMessage(messageRequest.getToEmail(), messageRequest.getBody(), messageRequest.getSubject());
    }

    @Test
    void whenScheduledWithBothPhoneAndEmail_thenBothServicesCalledAfterScheduleDelay() {
        MessageRequest messageRequest = MessageRequest.builder()
                .toPhone("8005551212")
                .toEmail("foo@example.com")
                .subject("Test subject")
                .body("Test message")
                .sendAt(OffsetDateTime.now().plusSeconds(20))
                .build();

        SmsMessage smsMessage = SmsMessage.builder()
                .body(messageRequest.getBody())
                .toPhone(messageRequest.getToPhone())
                .fromPhone("1234567890")
                .status("accepted")
                .providerMessageId("PROVIDER_MESSAGE_ID")
                .build();
        smsMessageRepository.save(smsMessage);

        Mockito.when(smsService.sendSmsMessage(Mockito.any(), Mockito.any()))
                .thenReturn(smsMessage);

        EmailMessage emailMessage = EmailMessage.builder()
                .body(messageRequest.getBody())
                .subject(messageRequest.getSubject())
                .toEmail(messageRequest.getToEmail())
                .fromEmail("messaging@example.com")
                .status("accepted")
                .providerMessageId("PROVIDER_MESSAGE_ID")
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

}
