package org.codeforamerica.messaging.services;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.exceptions.*;
import org.codeforamerica.messaging.jobs.SendMessageBatchJobRequest;
import org.codeforamerica.messaging.jobs.SendMessageJobRequest;
import org.codeforamerica.messaging.models.*;
import org.codeforamerica.messaging.repositories.MessageBatchRepository;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.utils.CSVReader;
import org.jobrunr.jobs.JobId;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;
import static org.codeforamerica.messaging.exceptions.MissingRecipientInfoHeadersException.RECIPIENT_INFO_HEADERS;
import static org.codeforamerica.messaging.models.TemplateVariant.checkForMissingPlaceholders;
import static org.codeforamerica.messaging.utils.CSVReader.*;


@Service
@Slf4j
public class MessageService implements MessageSourceAware {
    private final SmsService smsService;
    private final EmailService emailService;
    private final MessageRepository messageRepository;
    private final MessageBatchRepository messageBatchRepository;
    private final TemplateService templateService;
    private final JobRequestScheduler jobRequestScheduler;
    private MessageSource messageSource;

    public MessageService(SmsService smsService,
            EmailService emailService,
            MessageRepository messageRepository,
            MessageBatchRepository messageBatchRepository,
            TemplateService templateService,
            JobRequestScheduler jobRequestScheduler) {
        this.smsService = smsService;
        this.emailService = emailService;
        this.messageRepository = messageRepository;
        this.messageBatchRepository = messageBatchRepository;
        this.templateService = templateService;
        this.jobRequestScheduler = jobRequestScheduler;
    }

    @Override
    public void setMessageSource(@Nullable MessageSource messageSource) {
        this.messageSource = messageSource;
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

    public MessageBatch enqueueMessageBatch(MessageBatchRequest messageBatchRequest) {
        Template template = templateService.getTemplateByName(messageBatchRequest.getTemplateName());
        byte[] recipients;
        CSVReader csvReader;
        try {
            recipients = messageBatchRequest.getRecipients().getBytes();
            csvReader = new CSVReader(new InputStreamReader(new ByteArrayInputStream(recipients)));
        } catch (IOException e) {
            throw new InvalidRecipientsFileException(e);
        }

        List<String> csvHeaderNames = csvReader.getHeaderNames();
        if (RECIPIENT_INFO_HEADERS.stream().noneMatch(csvHeaderNames::contains)) {
            throw new MissingRecipientInfoHeadersException(RECIPIENT_INFO_HEADERS);
        }
        Set<String> missingTemplatePlaceholders = template.getAllPlaceholders().stream()
                .filter(not(csvHeaderNames::contains))
                .collect(Collectors.toSet());
        if (!missingTemplatePlaceholders.isEmpty()) {
            throw new MissingTemplateHeadersException(missingTemplatePlaceholders);
        }

        MessageBatch messageBatch = MessageBatch.builder()
                .template(template)
                .recipients(recipients)
                .sendAt(messageBatchRequest.getSendAt())
                .build();
        messageBatchRepository.save(messageBatch);
        JobId id = jobRequestScheduler.enqueue(new SendMessageBatchJobRequest(messageBatch.getId()));
        log.info("Enqueued SendMessageBatch job {}", id);
        return messageBatch;
    }

    public Message saveMessage(MessageRequest messageRequest, MessageBatch messageBatch) {
        TemplateVariant templateVariant = getTemplateVariant(messageRequest);
        checkForMissingPlaceholders(templateVariant.getAllPlaceholders(), messageRequest.getTemplateParams());
        Message message = Message.builder()
                .templateVariant(templateVariant)
                .templateParams(messageRequest.getTemplateParams())
                .toPhone(messageRequest.getToPhone())
                .toEmail(messageRequest.getToEmail())
                .messageBatch(messageBatch)
                .build();
        return messageRepository.save(message);
    }

    @Transactional
    public void sendMessage(Long messageId) {
        log.info("Sending message #{}", messageId);
        Message message = messageRepository.findById(messageId).orElseThrow();
        TemplateVariant templateVariant = message.getTemplateVariant();
        Map<String, String> templateParams = message.getTemplateParams();
        if (message.needToSendSms()) {
            try {
                String smsBody = templateVariant.build(TemplateVariant::getSmsBody, templateParams);
                SmsMessage sentSmsMessage = this.smsService.sendSmsMessage(message.getToPhone(), smsBody);
                log.info("Sending sms for message #{}, providerMessageId: {}", messageId, sentSmsMessage.getProviderMessageId());
                message.setSmsMessage(sentSmsMessage);
                message.setSmsStatus(MessageStatus.submission_succeeded);
                messageRepository.save(message);
            } catch (UnsubscribedException e) {
                message.setSmsStatus(MessageStatus.unsubscribed);
                message.setSmsErrorMessage(e.getMessage());
                messageRepository.save(message);
            } catch (Exception e) {
                log.error("Error sending SMS", e);
                message.setSmsStatus(MessageStatus.submission_failed);
                message.setSmsErrorMessage(e.getMessage());
                messageRepository.save(message);
            }
        }
        if (message.needToSendEmail()) {
            try {
                String subject = templateVariant.build(TemplateVariant::getSubject, templateParams);
                String emailBody = templateVariant.build(TemplateVariant::getEmailBody, templateParams);
                emailBody = addUnsubscribeFooter(message, emailBody);
                EmailMessage sentEmailMessage = this.emailService.sendEmailMessage(message.getToEmail(), emailBody, subject);
                log.info("Sending email for message #{}, providerMessageId: {}", messageId, sentEmailMessage.getProviderMessageId());
                message.setEmailMessage(sentEmailMessage);
                message.setEmailStatus(MessageStatus.submission_succeeded);
                messageRepository.save(message);
            } catch (UnsubscribedException e) {
                message.setEmailStatus(MessageStatus.unsubscribed);
                message.setEmailErrorMessage(e.getMessage());
                messageRepository.save(message);
            } catch (Exception e) {
                log.error("Error sending email", e);
                message.setEmailStatus(MessageStatus.submission_failed);
                message.setEmailErrorMessage(e.getMessage());
                messageRepository.save(message);
            }
        }
    }

    private String addUnsubscribeFooter(Message message, String emailBody) {
        String emailUnsubscribeFooter = messageSource.getMessage("email.unsubscribe.footer", null, Locale.forLanguageTag(message.getLanguage()));
        emailBody += "\n\n\n" + emailUnsubscribeFooter;
        return emailBody;
    }

    public Optional<Message> getMessage(Long id) {
        return messageRepository.findById(id);
    }

    public Optional<MessageBatch> getMessageBatch(Long id) {
        Optional<MessageBatch> messageBatch = messageBatchRepository.findById(id);
        messageBatch.ifPresent(batch -> batch.setMetrics(messageRepository.getMetrics(id)));
        return messageBatch;
    }

    public TemplateVariant getTemplateVariant(MessageRequest messageRequest) {
        Template template = templateService.getTemplateByName(messageRequest.getTemplateName());
        String language = messageRequest.getLanguage();
        String treatment = messageRequest.getTreatment();
        return template.getTemplateVariants().stream()
                .filter(tv -> language.equals(tv.getLanguage()))
                .filter(tv -> treatment.equals(tv.getTreatment()))
                .findFirst()
                .orElseThrow(() -> new ElementNotFoundException(
                        "TemplateVariant not found: name=%s; language=%s; treatment=%s"
                                .formatted(messageRequest.getTemplateName(), language, treatment)));
    }

    public void scheduleMessagesInBatch(Long messageBatchId)  {
        MessageBatch messageBatch = messageBatchRepository.findById(messageBatchId).orElseThrow(() -> {
            log.error("Could not find batch #{} after being scheduled", messageBatchId);
            return null;
        });
        CSVReader csvReader;
        try {
            csvReader = new CSVReader(new InputStreamReader(new ByteArrayInputStream(messageBatch.getRecipients())));
        } catch (IOException e) {
            log.error("Could not read recipients file in batch #{} after being scheduled", messageBatchId, e);
            return;
        }

        log.info("Scheduling messages in batch #{}", messageBatchId);
        List<Map<String, String>> recipientErrorRows = new LinkedList<>();
        csvReader.stream().forEach(row -> {
            MessageRequest messageRequest = MessageRequest.builder()
                    .toPhone(PhoneNumber.valueOf(row.get(PHONE_HEADER)))
                    .toEmail(row.get(EMAIL_HEADER))
                    .templateName(messageBatch.getTemplate().getName())
                    .templateParams(row)
                    .sendAt(messageBatch.getSendAt())
                    .build();

            try {
                this.scheduleMessage(messageRequest, messageBatch);
            } catch (Exception e) {
                row.put(ERROR_HEADER, e.getMessage());
                recipientErrorRows.add(row);
            }
        });
        messageBatch.setRecipientErrorRows(recipientErrorRows);
        messageBatchRepository.save(messageBatch);
    }
}
