package org.codeforamerica.messaging.services;

import org.codeforamerica.messaging.models.SmsMessage;
import org.codeforamerica.messaging.providers.twilio.TwilioGateway;
import org.codeforamerica.messaging.repositories.SmsMessageRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SmsService {
    private final TwilioGateway twilioGateway;
    private final SmsMessageRepository smsMessageRepository;
    private final TemplateService templateService;

    public SmsService(TwilioGateway twilioGateway, SmsMessageRepository smsMessageRepository, TemplateService templateService) {
        this.twilioGateway = twilioGateway;
        this.smsMessageRepository = smsMessageRepository;
        this.templateService = templateService;
    }

    public SmsMessage sendSmsMessage(String to, String templateName, Map<String, Object> templateParams) {
        String body = templateService.buildBodyFromTemplate(templateName, templateParams);
        SmsMessage smsMessage = twilioGateway.sendMessage(to, body);
        smsMessage = smsMessageRepository.save(smsMessage);
        return smsMessage;
    }
}
