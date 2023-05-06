package org.codeforamerica.messaging.services;

import org.codeforamerica.messaging.models.SmsMessage;
import org.codeforamerica.messaging.models.Template;
import org.codeforamerica.messaging.providers.twilio.TwilioGateway;
import org.codeforamerica.messaging.repositories.SmsMessageRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SmsService {
    private final TwilioGateway twilioGateway;
    private final SmsMessageRepository smsMessageRepository;

    public SmsService(TwilioGateway twilioGateway, SmsMessageRepository smsMessageRepository) {
        this.twilioGateway = twilioGateway;
        this.smsMessageRepository = smsMessageRepository;
    }

    public SmsMessage sendSmsMessage(String to, Template template, Map<String, Object> templateParams) {
        String body = template.buildBodyFromTemplate(templateParams);
        SmsMessage smsMessage = twilioGateway.sendMessage(to, body);
        smsMessage = smsMessageRepository.save(smsMessage);
        return smsMessage;
    }
}
