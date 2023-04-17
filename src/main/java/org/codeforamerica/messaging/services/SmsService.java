package org.codeforamerica.messaging.services;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.models.Message;
import org.codeforamerica.messaging.providers.twilio.TwilioGateway;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class SmsService {

    private final TwilioGateway twilioGateway;

    private final MessageRepository messageRepository;

    public SmsService(TwilioGateway twilioGateway, MessageRepository messageRepository) {
        this.twilioGateway = twilioGateway;
        this.messageRepository = messageRepository;
    }

    public Message sendSmsMessage(String to, String body) {
        Message message = twilioGateway.sendMessage(to, body);
        message = messageRepository.save(message);
        log.info("Message sent, Twilio response: " + message);
        return message;
    }

    public Optional<Message> getMessage(Long id) {
        return messageRepository.findById(id);
    }

}
