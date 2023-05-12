package org.codeforamerica.messaging.services;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.models.EmailMessage;
import org.codeforamerica.messaging.models.Message;
import org.codeforamerica.messaging.models.MessageRequest;
import org.codeforamerica.messaging.models.SmsMessage;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.jobrunr.jobs.JobId;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;


@Service
@Slf4j
public class MessageService {
    private final SmsService smsService;
    private final EmailService emailService;
    private final MessageRepository messageRepository;
    private final JobRequestScheduler jobRequestScheduler;

    public MessageService(SmsService smsService, EmailService emailService, MessageRepository messageRepository,
            JobRequestScheduler jobRequestScheduler) {
        this.smsService = smsService;
        this.emailService = emailService;
        this.messageRepository = messageRepository;
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
        Message message = Message.builder()
                .subject(messageRequest.getSubject())
                .body(messageRequest.getBody())
                .toPhone(messageRequest.getToPhone())
                .toEmail(messageRequest.getToEmail())
                .build();
        messageRepository.save(message);
        return message;
    }

    public void sendMessage(Long messageId) throws Exception {
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

}
