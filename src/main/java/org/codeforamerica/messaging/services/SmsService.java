package org.codeforamerica.messaging.services;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.models.SmsMessage;
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

    public SmsMessage sendSmsMessage(String to, String body) {
        SmsMessage smsMessage = twilioGateway.sendMessage(to, body);
        log.info("Message sent, Twilio response: " + smsMessage);
        smsMessage = messageRepository.save(smsMessage);
        return smsMessage;
    }

    public Optional<SmsMessage> getMessage(Long id) {
        return messageRepository.findById(id);
    }

}
