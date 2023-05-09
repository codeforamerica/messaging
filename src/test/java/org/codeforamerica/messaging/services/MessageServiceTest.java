package org.codeforamerica.messaging.services;

import org.codeforamerica.messaging.models.Message;
import org.codeforamerica.messaging.models.MessageRequest;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

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
    void whenScheduled_thenSendMessageCalled() {
        MessageRequest messageRequest = MessageRequest.builder()
                .toPhone("8005551212")
                .toEmail("foo@example.com")
                .subject("Test subject")
                .body("Test message")
                .build();

        messageService.scheduleMessage(messageRequest);
        Mockito.verify(smsService).sendSmsMessage(messageRequest.getToPhone(), messageRequest.getBody());
        Mockito.verify(emailService).sendEmailMessage(messageRequest.getToEmail(), messageRequest.getBody(), messageRequest.getSubject());
    }
}
