package org.codeforamerica.messaging.services;

import org.codeforamerica.messaging.models.SmsMessage;
import org.codeforamerica.messaging.providers.twilio.TwilioGateway;
import org.codeforamerica.messaging.repositories.SmsMessageRepository;
import org.springframework.stereotype.Service;

@Service
public class SmsService {
    private final TwilioGateway twilioGateway;
    private final SmsMessageRepository smsMessageRepository;

    public SmsService(TwilioGateway twilioGateway, SmsMessageRepository smsMessageRepository) {
        this.twilioGateway = twilioGateway;
        this.smsMessageRepository = smsMessageRepository;
    }

    public SmsMessage sendSmsMessage(SmsMessage smsMessage) {
        return smsMessageRepository.save(twilioGateway.sendMessage(smsMessage));
    }
}
