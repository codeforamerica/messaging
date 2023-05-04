package org.codeforamerica.messaging.services;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.models.EmailMessage;
import org.codeforamerica.messaging.models.Message;
import org.codeforamerica.messaging.models.MessageRequest;
import org.codeforamerica.messaging.models.SmsMessage;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@Slf4j
public class MessageService {
    private final SmsService smsService;
    private final EmailService emailService;
    private final MessageRepository messageRepository;


    public MessageService(SmsService smsService, EmailService emailService, MessageRepository messageRepository) {
        this.smsService = smsService;
        this.emailService = emailService;
        this.messageRepository = messageRepository;
    }

    public Message sendMessage(MessageRequest messageRequest) {
        Message message = Message.builder()
                .subject(messageRequest.getSubject())
                .body(messageRequest.getBody())
                .toPhone(messageRequest.getToPhone())
                .toEmail(messageRequest.getToEmail())
                .build();
        SmsMessage sentSmsMessage;
        EmailMessage sentEmailMessage;
        if (message.getToPhone() != null) {
            sentSmsMessage = this.smsService.sendSmsMessage(message.getToPhone(), message.getBody());
            message.setSmsMessage(sentSmsMessage);
        }
        if (message.getToEmail() != null) {
            sentEmailMessage = this.emailService.sendEmailMessage(message.getToEmail(), message.getBody(), message.getSubject());
            message.setEmailMessage(sentEmailMessage);
        }

        messageRepository.save(message);
        return message;
    }


    public Optional<Message> getMessage(Long id) {
        return messageRepository.findById(id);
    }

}
