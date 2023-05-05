package org.codeforamerica.messaging.services;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.models.*;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.repositories.TemplateSetRepository;
import org.codeforamerica.messaging.repositories.TemplateVariantRepository;
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
    private final TemplateSetRepository templateSetRepository;
    private final TemplateVariantRepository templateVariantRepository;
    private final JobRequestScheduler jobRequestScheduler;


    public MessageService(SmsService smsService,
            EmailService emailService,
            MessageRepository messageRepository,
            TemplateSetRepository templateSetRepository,
            TemplateVariantRepository templateVariantRepository,
            JobRequestScheduler jobRequestScheduler) {
        this.smsService = smsService;
        this.emailService = emailService;
        this.messageRepository = messageRepository;
        this.templateSetRepository = templateSetRepository;
        this.templateVariantRepository = templateVariantRepository;
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
            }
            if (message.needToSendEmail()) {
                EmailMessage sentEmailMessage = this.emailService.sendEmailMessage(message.getToEmail(), message.getBody(), message.getSubject());
                message.setEmailMessage(sentEmailMessage);
            }
            messageRepository.save(message);
        } catch (Exception e) {
            log.error("Error running job", e);
            throw e;
        }
    }

    public Optional<Message> getMessage(Long id) {
        return messageRepository.findById(id);
    }

    private TemplateVariant getTemplateVariant(MessageRequest messageRequest) {
        TemplateSet templateSet = templateSetRepository.findFirstByNameIgnoreCase(messageRequest.getTemplateName().strip());
        if (templateSet == null) {
            throw new RuntimeException(String.format(
                    "Template Set not found with the name provided: name=%s", messageRequest.getTemplateName()));
        }
        String language = messageRequest.getLanguage();
        String treatment = messageRequest.getTreatment();
        TemplateVariant templateVariant = templateVariantRepository.findFirstByTemplateSetAndLanguageAndTreatment(
                templateSet, language, treatment);
        if (templateVariant == null) {
            throw new RuntimeException(String.format(
                    "Template Variant not found with the info provided: name=%s, language=%s, treatment=%s",
                    messageRequest.getTemplateName(), language, treatment));
        }
        return templateVariant;
    }

}
