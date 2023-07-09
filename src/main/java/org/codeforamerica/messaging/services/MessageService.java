package org.codeforamerica.messaging.services;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.jobs.SendMessageBatchJobRequest;
import org.codeforamerica.messaging.jobs.SendMessageJobRequest;
import org.codeforamerica.messaging.models.*;
import org.codeforamerica.messaging.repositories.MessageBatchRepository;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.repositories.TemplateRepository;
import org.codeforamerica.messaging.utils.CSVReader;
import org.jobrunr.jobs.JobId;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;


@Service
@Slf4j
public class MessageService {
    private final SmsService smsService;
    private final EmailService emailService;
    private final MessageRepository messageRepository;
    private final MessageBatchRepository messageBatchRepository;
    private final TemplateRepository templateRepository;
    private final JobRequestScheduler jobRequestScheduler;
//    @Autowired
//    private final MessageValidator messageValidator;


    public MessageService(SmsService smsService,
            EmailService emailService,
            MessageRepository messageRepository,
            MessageBatchRepository messageBatchRepository,
            TemplateRepository templateRepository,
            JobRequestScheduler jobRequestScheduler) {
        this.smsService = smsService;
        this.emailService = emailService;
        this.messageRepository = messageRepository;
        this.messageBatchRepository = messageBatchRepository;
        this.templateRepository = templateRepository;
        this.jobRequestScheduler = jobRequestScheduler;
    }

    public Message scheduleMessage(MessageRequest messageRequest, MessageBatch messageBatch) {
        Message message = saveMessage(messageRequest, messageBatch);

        OffsetDateTime sendAt = messageRequest.getSendAt() == null ? OffsetDateTime.now() : messageRequest.getSendAt();
        JobId id = jobRequestScheduler.schedule(sendAt, new SendMessageJobRequest(message.getId()));
        log.info("Scheduled SendMessage job {} to send at {}", id, sendAt);
        return message;
    }

    public Message scheduleMessage(MessageRequest messageRequest) {
        return scheduleMessage(messageRequest, null);
    }

    public Message scheduleMessage(MessageBatch messageBatch, Map<String, String> recipient) {
        MessageRequest messageRequest = MessageRequest.builder()
                .toPhone(recipient.get("phone"))
                .toEmail(recipient.get("email"))
                .templateName(messageBatch.getTemplate().getName())
                .templateParams(recipient)
                .sendAt(messageBatch.getSendAt())
                .build();
        return scheduleMessage(messageRequest, messageBatch);
    }

    public MessageBatch enqueueMessageBatch(MessageBatchRequest messageBatchRequest) throws IOException {
        Template template = templateRepository.findFirstByNameIgnoreCase(messageBatchRequest.getTemplateName()).orElseThrow(() -> new RuntimeException("template not found"));
        MessageBatch messageBatch = MessageBatch.builder()
                .template(template)
                .recipients(messageBatchRequest.getRecipients().getBytes())
                .sendAt(messageBatchRequest.getSendAt())
                .build();
        messageBatchRepository.save(messageBatch);
        JobId id = jobRequestScheduler.enqueue(new SendMessageBatchJobRequest(messageBatch.getId()));
        log.info("Enqueued SendMessageBatch job {}", id);
        return messageBatch;
    }

    public Message saveMessage(MessageRequest messageRequest, MessageBatch messageBatch) {
        TemplateVariant templateVariant = getTemplateVariant(messageRequest);
        String subject;
        String emailBody;
        String smsBody;
        try {
            subject = templateVariant.build(TemplateVariant::getSubject, messageRequest.getTemplateParams());
            emailBody = templateVariant.build(TemplateVariant::getEmailBody, messageRequest.getTemplateParams());
            smsBody = templateVariant.build(TemplateVariant::getSmsBody, messageRequest.getTemplateParams());
        } catch (IOException e) {
            log.error("Error processing templates. " + templateVariant);
            throw new RuntimeException(e.getMessage());
        }
        Message message = Message.builder()
                .templateVariant(templateVariant)
                .subject(subject)
                .emailBody(emailBody)
                .smsBody(smsBody)
                .toPhone(messageRequest.getToPhone())
                .toEmail(messageRequest.getToEmail())
                .messageBatch(messageBatch)
                .build();
        return messageRepository.save(message);
    }

    public void sendMessage(Long messageId) {
        log.info("Sending message #{}", messageId);
        try {
            Message message = messageRepository.findById(messageId).get();
            if (message.needToSendSms()) {
                SmsMessage sentSmsMessage = this.smsService.sendSmsMessage(message.getToPhone(), message.getSmsBody());
                message.setSmsMessage(sentSmsMessage);
                messageRepository.save(message);
            }
            if (message.needToSendEmail()) {
                EmailMessage sentEmailMessage = this.emailService.sendEmailMessage(message.getToEmail(), message.getEmailBody(), message.getSubject());
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

    public Optional<MessageBatch> getMessageBatch(Long id) {
        Optional<MessageBatch> messageBatch = messageBatchRepository.findById(id);
        if (messageBatch.isPresent()) {
            messageBatch.get().setMetrics(messageRepository.getMetrics(id));
        }
        return messageBatch;
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

    public void scheduleMessageBatch(Long messageBatchId) throws IOException {
        MessageBatch messageBatch = messageBatchRepository.findById(messageBatchId).orElseThrow();
        CSVReader csvReader = new CSVReader(new InputStreamReader(new ByteArrayInputStream(messageBatch.getRecipients())));
        csvReader.stream().forEach((r) -> this.scheduleMessage(messageBatch, r) );
    }
}
