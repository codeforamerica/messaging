package org.codeforamerica.messaging.services;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.exceptions.MessageSendException;
import org.codeforamerica.messaging.models.MessageStatus;
import org.codeforamerica.messaging.models.SmsMessage;
import org.codeforamerica.messaging.providers.twilio.TwilioGateway;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.repositories.SmsMessageRepository;
import org.jobrunr.JobRunrException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class SmsService {

    private final TwilioGateway twilioGateway;
    private final SmsMessageRepository smsMessageRepository;
    private final MessageRepository messageRepository;

    public SmsService(TwilioGateway twilioGateway, SmsMessageRepository smsMessageRepository, MessageRepository messageRepository) {
        this.twilioGateway = twilioGateway;
        this.smsMessageRepository = smsMessageRepository;
        this.messageRepository = messageRepository;
    }

    public SmsMessage sendSmsMessage(String to, String body) throws MessageSendException {
        SmsMessage smsMessage = twilioGateway.sendMessage(to, body);
        return smsMessageRepository.save(smsMessage);
    }

    @Transactional
    public void updateStatus(String providerMessageId, MessageStatus newSmsStatus, String rawStatus, String fromPhone,
            Map<String, String> providerError) {
        SmsMessage smsMessage = smsMessageRepository.findFirstByProviderMessageId(providerMessageId);
        if (smsMessage == null) {
            log.error("Cannot find message with providerId: {}", providerMessageId);
            throw new JobRunrException("Cannot find message with providerId");
        }
        log.info("Updating status for {}", providerMessageId);
        MessageStatus currentSmsStatus = smsMessage.getMessage().getSmsStatus();
        if (newSmsStatus.isAfter(currentSmsStatus)) {
            log.info("Updating status. Provider message id: {}, current status: {}, new status: {}", providerMessageId,
                    currentSmsStatus, newSmsStatus);
            smsMessage.getMessage().setRawSmsStatus(rawStatus);
            smsMessage.getMessage().setSmsStatus(newSmsStatus);
            smsMessage.setFromPhone(fromPhone);
            smsMessage.setProviderError(providerError);
            smsMessageRepository.save(smsMessage);
            messageRepository.save(smsMessage.getMessage());
        } else {
            log.info("Ignoring earlier status {}, current status: {}", newSmsStatus, currentSmsStatus);
        }
    }
}
