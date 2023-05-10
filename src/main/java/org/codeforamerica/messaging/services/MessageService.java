package org.codeforamerica.messaging.services;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.models.*;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.repositories.TemplateRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@Slf4j
public class MessageService {
    private final SmsService smsService;
    private final EmailService emailService;
    private final MessageRepository messageRepository;
    private final TemplateRepository templateRepository;


    public MessageService(SmsService smsService,
            EmailService emailService,
            MessageRepository messageRepository,
            TemplateRepository templateRepository) {
        this.smsService = smsService;
        this.emailService = emailService;
        this.messageRepository = messageRepository;
        this.templateRepository = templateRepository;
    }

    public Message sendMessage(MessageRequest messageRequest) {
        Template template = templateRepository.findFirstByName(messageRequest.getTemplateName());
        if (template == null) {
            throw new RuntimeException("Template not found with the name provided");
        }

        Message message = Message.builder()
                .toPhone(messageRequest.getToPhone())
                .toEmail(messageRequest.getToEmail())
                .template(template)
                .build();

        String subject = template.build(Template::getSubject, messageRequest.getTemplateParams());
        String body = template.build(Template::getBody, messageRequest.getTemplateParams());
        if (message.getToPhone() != null) {
            SmsMessage sentSmsMessage = this.smsService.sendSmsMessage(message.getToPhone(), body);
            message.setSmsMessage(sentSmsMessage);
        }
        if (message.getToEmail() != null) {
            EmailMessage sentEmailMessage = this.emailService.sendEmailMessage(message.getToEmail(), body, subject);
            message.setEmailMessage(sentEmailMessage);
        }

        messageRepository.save(message);
        return message;
    }


    public Optional<Message> getMessage(Long id) {
        return messageRepository.findById(id);
    }

}
