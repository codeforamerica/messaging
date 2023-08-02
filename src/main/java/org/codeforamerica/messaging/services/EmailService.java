package org.codeforamerica.messaging.services;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.exceptions.MessageSendException;
import org.codeforamerica.messaging.exceptions.UnsubscribedException;
import org.codeforamerica.messaging.models.EmailMessage;
import org.codeforamerica.messaging.models.EmailSubscription;
import org.codeforamerica.messaging.providers.mailgun.MailgunGateway;
import org.codeforamerica.messaging.repositories.EmailMessageRepository;
import org.codeforamerica.messaging.repositories.EmailSubscriptionRepository;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final MailgunGateway mailgunGateway;

    private final EmailMessageRepository emailMessageRepository;
    private final EmailSubscriptionRepository emailSubscriptionRepository;

    public EmailService(MailgunGateway mailgunGateway, EmailMessageRepository emailMessageRepository, EmailSubscriptionRepository emailSubscriptionRepository) {
        this.mailgunGateway = mailgunGateway;
        this.emailMessageRepository = emailMessageRepository;
        this.emailSubscriptionRepository = emailSubscriptionRepository;
    }

    public EmailMessage sendEmailMessage (String toEmail, String body, String subject) throws MessageSendException {
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
}
