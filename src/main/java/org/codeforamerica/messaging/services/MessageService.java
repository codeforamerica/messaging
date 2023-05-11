package org.codeforamerica.messaging.services;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.models.*;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.repositories.TemplateRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.codeforamerica.messaging.models.Template.DEFAULT_LANGUAGE;
import static org.codeforamerica.messaging.models.Template.DEFAULT_VARIANT;


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
        String language = getLanguage(messageRequest);
        String variant = getVariant(messageRequest);
        Template template = templateRepository.findFirstByNameAndLanguageAndVariant(
                messageRequest.getTemplateName(), language, variant);
        if (template == null) {
            throw new RuntimeException(String.format(
                    "Template not found with the info provided: name=%s, language=%s, variant=%s",
                    messageRequest.getTemplateName(), language, variant));
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

    private String getLanguage(MessageRequest messageRequest) {
        if (messageRequest.getTemplateParams() != null && messageRequest.getTemplateParams().get("language") != null) {
            return messageRequest.getTemplateParams().get("language").toString();
        }
        return DEFAULT_LANGUAGE;
    }

    private String getVariant(MessageRequest messageRequest) {
        if (messageRequest.getTemplateParams() != null && messageRequest.getTemplateParams().get("variant") != null) {
            return messageRequest.getTemplateParams().get("variant").toString();
        }
        return DEFAULT_VARIANT;
    }

}
