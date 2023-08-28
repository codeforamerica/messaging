package org.codeforamerica.messaging.services;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.exceptions.MessageSendException;
import org.codeforamerica.messaging.exceptions.UnsubscribedException;
import org.codeforamerica.messaging.models.*;
import org.codeforamerica.messaging.providers.twilio.TwilioGateway;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.repositories.SmsMessageRepository;
import org.codeforamerica.messaging.repositories.SmsSubscriptionRepository;
import org.jobrunr.JobRunrException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class SmsService {

    private final TwilioGateway twilioGateway;
    private final SmsMessageRepository smsMessageRepository;
    private final SmsSubscriptionRepository smsSubscriptionRepository;
    private final MessageRepository messageRepository;

    public SmsService(TwilioGateway twilioGateway, SmsMessageRepository smsMessageRepository,
            SmsSubscriptionRepository smsSubscriptionRepository, MessageRepository messageRepository) {
        this.twilioGateway = twilioGateway;
        this.smsMessageRepository = smsMessageRepository;
        this.smsSubscriptionRepository = smsSubscriptionRepository;
        this.messageRepository = messageRepository;
    }

    public SmsMessage sendSmsMessage(PhoneNumber toPhone, String body) throws MessageSendException {
        SmsMessage message;
        if (!unsubscribed(toPhone)) {
            message = twilioGateway.sendMessage(toPhone.getNumber(), body);
            message = smsMessageRepository.save(message);
        } else {
            log.error("Skipping unsubscribed phone");
            throw new UnsubscribedException();
        }
        return message;
    }

    private boolean unsubscribed(PhoneNumber toPhone) {
        SmsSubscription latestSubscription = smsSubscriptionRepository.findFirstByPhoneNumberOrderByCreationTimestampDesc(toPhone);
        return latestSubscription != null && latestSubscription.isUnsubscribed();
    }

    public void subscribe(PhoneNumber phoneNumber) {
        log.info("Subscribing");
        saveSubscription(phoneNumber, false);
    }

    public void unsubscribe(PhoneNumber phoneNumber) {
        log.info("Unsubscribing");
        saveSubscription(phoneNumber, true);
    }

    private void saveSubscription(PhoneNumber phoneNumber, boolean unsubscribed) {
        smsSubscriptionRepository.save(SmsSubscription.builder()
                .phoneNumber(phoneNumber)
                .sourceInternal(true)
                .unsubscribed(unsubscribed)
                .build());
    }

    @Transactional
    public void updateStatus(String providerMessageId, MessageStatus newSmsStatus, String rawStatus, PhoneNumber fromPhone,
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
