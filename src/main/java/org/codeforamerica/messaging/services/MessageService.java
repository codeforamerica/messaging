package org.codeforamerica.messaging.services;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.models.*;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.repositories.TemplateRepository;
import org.jobrunr.jobs.JobId;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Optional;


@Service
@Slf4j
public class MessageService {
    private final SmsService smsService;
    private final EmailService emailService;
    private final MessageRepository messageRepository;
    private final TemplateRepository templateRepository;
    private final JobRequestScheduler jobRequestScheduler;


    public MessageService(SmsService smsService,
            EmailService emailService,
            MessageRepository messageRepository,
            TemplateRepository templateRepository,
            JobRequestScheduler jobRequestScheduler) {
        this.smsService = smsService;
        this.emailService = emailService;
        this.messageRepository = messageRepository;
        this.templateRepository = templateRepository;
        this.jobRequestScheduler = jobRequestScheduler;
    }

    public Message scheduleMessage(MessageRequest messageRequest) {
        Message message = saveMessage(messageRequest);

        OffsetDateTime sendAt = messageRequest.getSendAt() == null ? OffsetDateTime.now() : messageRequest.getSendAt();
        JobId id = jobRequestScheduler.schedule(sendAt, new SendMessageJobRequest(message.getId()));
        log.info("Scheduled job {} to run at {}", id, sendAt);
        return message;
    }

    public Message saveMessage(MessageRequest messageRequest) {
        TemplateVariant templateVariant = getTemplateVariant(messageRequest);
        String subject;
        String body;
        try {
            subject = templateVariant.build(TemplateVariant::getSubject, messageRequest.getTemplateParams());
            body = templateVariant.build(TemplateVariant::getBody, messageRequest.getTemplateParams());
        } catch (IOException e) {
            log.error("Error processing templates. " + templateVariant);
            throw new RuntimeException(e.getMessage());
        }
        Message message = Message.builder()
                .templateVariant(templateVariant)
                .subject(subject)
                .body(body)
                .toPhone(messageRequest.getToPhone())
                .toEmail(messageRequest.getToEmail())
                .build();
        messageRepository.save(message);
        return message;
    }

    public void sendMessage(Long messageId) {
        log.info("Sending message #{}", messageId);
        try {
            Message message = messageRepository.findById(messageId).get();
            if (message.needToSendSms()) {
                SmsMessage sentSmsMessage = this.smsService.sendSmsMessage(message.getToPhone(), message.getBody());
                message.setSmsMessage(sentSmsMessage);
                messageRepository.save(message);
            }
            if (message.needToSendEmail()) {
                EmailMessage sentEmailMessage = this.emailService.sendEmailMessage(message.getToEmail(), message.getBody(), message.getSubject());
                message.setEmailMessage(sentEmailMessage);
                messageRepository.save(message);
            }
        } catch (Exception e) {
            log.error("Error running job", e);
            throw e;
        }
    }

    public Optional<Message> getMessage(Long id) {
        return messageRepository.findById(id);
    }

    private TemplateVariant getTemplateVariant(MessageRequest messageRequest) {
        Optional<Template> templateOptional = templateRepository.findFirstByNameIgnoreCase(messageRequest.getTemplateName().strip());
        if (templateOptional.isEmpty()) {
            throw new RuntimeException(String.format(
                    "Template not found with the name provided: name=%s", messageRequest.getTemplateName()));
        }
        String language = messageRequest.getLanguage();
        String treatment = messageRequest.getTreatment();
        Optional<TemplateVariant> templateVariantOptional = templateOptional.get().getTemplateVariants().stream()
                .filter(templateVariant -> language.equals(templateVariant.getLanguage()))
                .filter(templateVariant -> treatment.equals(templateVariant.getTreatment()))
                .findFirst();
        if (templateVariantOptional.isEmpty()) {
            throw new RuntimeException(String.format(
                    "Template Variant not found with the info provided: name=%s, language=%s, treatment=%s",
                    messageRequest.getTemplateName(), language, treatment));
        }
        return templateVariantOptional.get();
    }

}
