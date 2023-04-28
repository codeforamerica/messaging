package org.codeforamerica.messaging.services;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.models.SmsMessage;
import org.codeforamerica.messaging.providers.twilio.TwilioGateway;
import org.codeforamerica.messaging.repositories.SmsMessageRepository;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    private final TwilioGateway twilioGateway;

    private final SmsMessageRepository smsMessageRepository;

    public SmsService(TwilioGateway twilioGateway, SmsMessageRepository smsMessageRepository) {
        this.twilioGateway = twilioGateway;
        this.smsMessageRepository = smsMessageRepository;
    }

    public SmsMessage sendSmsMessage(String to, String body) {
        SmsMessage smsMessage = twilioGateway.sendMessage(to, body);
        log.info("Message sent, Twilio response: " + smsMessage);
        smsMessage = smsMessageRepository.save(smsMessage);
        return smsMessage;
    }
}
