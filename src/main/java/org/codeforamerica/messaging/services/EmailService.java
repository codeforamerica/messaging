package org.codeforamerica.messaging.services;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.exceptions.MessageSendException;
import org.codeforamerica.messaging.exceptions.UnsubscribedException;
import org.codeforamerica.messaging.models.EmailMessage;
import org.codeforamerica.messaging.models.EmailSubscription;
import org.codeforamerica.messaging.models.MessageStatus;
import org.codeforamerica.messaging.providers.mailgun.MailgunGateway;
import org.codeforamerica.messaging.repositories.EmailMessageRepository;
import org.codeforamerica.messaging.repositories.EmailSubscriptionRepository;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.jobrunr.JobRunrException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class EmailService {

    private final MailgunGateway mailgunGateway;

    private final EmailMessageRepository emailMessageRepository;
    private final MessageRepository messageRepository;
    private final EmailSubscriptionRepository emailSubscriptionRepository;

    public EmailService(MailgunGateway mailgunGateway, EmailMessageRepository emailMessageRepository,
            MessageRepository messageRepository, EmailSubscriptionRepository emailSubscriptionRepository) {
        this.mailgunGateway = mailgunGateway;
        this.emailMessageRepository = emailMessageRepository;
        this.messageRepository = messageRepository;
        this.emailSubscriptionRepository = emailSubscriptionRepository;
    }

    public EmailMessage sendEmailMessage(String toEmail, String body, String subject) throws MessageSendException {
        EmailMessage message;
        if (!unsubscribed(toEmail)) {
            message = mailgunGateway.sendMessage(toEmail, body, subject);
            message = emailMessageRepository.save(message);
        } else {
            log.error("Skipping unsubscribed email");
            throw new UnsubscribedException();
        }
        return message;
    }

    private boolean unsubscribed(String toEmail) {
        EmailSubscription latestSubscription = emailSubscriptionRepository.findFirstByEmailOrderByCreationTimestampDesc(toEmail);
        return latestSubscription != null && latestSubscription.isUnsubscribed();
    }

    @Transactional
    public void updateStatus(String providerMessageId, MessageStatus newEmailStatus, String rawEmailStatus, Map<String, String> providerError) {
        EmailMessage emailMessage = emailMessageRepository.findFirstByProviderMessageId(providerMessageId);
        if (emailMessage == null) {
            log.error("Cannot find message with providerId: {}", providerMessageId);
            throw new JobRunrException("Cannot find message with providerId");
        }
        log.info("Updating status for {}", providerMessageId);
        MessageStatus currentEmailStatus = emailMessage.getMessage().getEmailStatus();
        if (newEmailStatus.isAfter(currentEmailStatus)) {
            log.info("Updating status. Provider message id: {}, current status: {}, new status: {}", providerMessageId, currentEmailStatus, newEmailStatus);
            emailMessage.getMessage().setRawEmailStatus(rawEmailStatus);
            emailMessage.getMessage().setEmailStatus(newEmailStatus);
            emailMessage.setProviderError(providerError);
            emailMessageRepository.save(emailMessage);
            messageRepository.save(emailMessage.getMessage());
        } else {
            log.info("Ignoring earlier status {}, current status: {}", newEmailStatus, currentEmailStatus);
        }

    }
}
