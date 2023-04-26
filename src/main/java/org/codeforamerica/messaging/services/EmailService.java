package org.codeforamerica.messaging.services;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.models.EmailMessage;
import org.codeforamerica.messaging.providers.mailgun.MailgunGateway;
import org.codeforamerica.messaging.repositories.EmailMessageRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class EmailService {

    private final MailgunGateway mailgunGateway;

    private final EmailMessageRepository emailMessageRepository;

    public EmailService(MailgunGateway mailgunGateway, EmailMessageRepository emailMessageRepository) {
        this.mailgunGateway = mailgunGateway;
        this.emailMessageRepository = emailMessageRepository;
    }

    public EmailMessage sendEmailMessage(String to, String body, String subject) {
        EmailMessage message = mailgunGateway.sendMessage(to, body, subject);
        message = emailMessageRepository.save(message);
        log.info("Message sent, Mailgun response: " + message);
        return message;
    }

    public Optional<EmailMessage> getMessage(Long id) {
        return emailMessageRepository.findById(id);
    }

}
