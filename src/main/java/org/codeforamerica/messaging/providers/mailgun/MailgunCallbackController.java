package org.codeforamerica.messaging.providers.mailgun;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.jobs.EmailMessageStatusUpdateJobRequest;
import org.codeforamerica.messaging.models.EmailSubscription;
import org.codeforamerica.messaging.models.MessageStatus;
import org.codeforamerica.messaging.repositories.EmailSubscriptionRepository;
import org.jobrunr.jobs.JobId;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/public/mailgun_callbacks")
@Slf4j
public class MailgunCallbackController {
    private final EmailSubscriptionRepository emailSubscriptionRepository;
    private final MailgunSignatureVerificationService mailgunSignatureVerificationService;
    private final JobRequestScheduler jobRequestScheduler;


    public MailgunCallbackController(EmailSubscriptionRepository emailSubscriptionRepository,
                                     MailgunSignatureVerificationService mailgunSignatureVerificationService,
            JobRequestScheduler jobRequestScheduler) {
        this.emailSubscriptionRepository = emailSubscriptionRepository;
        this.mailgunSignatureVerificationService = mailgunSignatureVerificationService;
        this.jobRequestScheduler = jobRequestScheduler;
    }

    @PostMapping(path = "/status")
    public ResponseEntity<Object> updateStatus(@RequestBody JsonNode requestJSON) {
        String providerMessageId = requestJSON.at("/event-data/message/headers/message-id").textValue();

        if (!mailgunSignatureVerificationService.verifySignature(requestJSON)) {
            log.error("Signature verification failed. Provider message id: {}", providerMessageId);
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }
        String rawEmailStatus = requestJSON.at("/event-data/event").textValue();
        if (rawEmailStatus.equals("unsubscribed")) {
            unsubscribeEmail(requestJSON);
        } else {
            enqueueStatusUpdate(requestJSON, providerMessageId, rawEmailStatus);
        }
        return ResponseEntity.ok().build();
    }

    private void enqueueStatusUpdate(JsonNode requestJSON, String providerMessageId,
            String rawEmailStatus) {
        MessageStatus newEmailStatus = mapMailgunStatustoMessageStatus(rawEmailStatus);
        Map<String, String> providerError = hadError(newEmailStatus)? buildProviderError(requestJSON, newEmailStatus): null;
        JobId id = jobRequestScheduler.enqueue(new EmailMessageStatusUpdateJobRequest(providerMessageId, rawEmailStatus, newEmailStatus, providerError));
    }

    private void unsubscribeEmail(JsonNode requestJSON) {
        String unsubscribedEmail = requestJSON.at("/event-data/recipient").textValue();
        log.info("Unsubscribing");
        emailSubscriptionRepository.save(EmailSubscription.builder()
                .email(unsubscribedEmail)
                .sourceInternal(true)
                .unsubscribed(true)
                .build());
    }

    private static boolean hadError(MessageStatus status) {
        return status == MessageStatus.failed || status == MessageStatus.undelivered;
    }

    private static Map<String, String> buildProviderError(JsonNode requestJSON, MessageStatus status) {
        Map<String, String> providerError = new HashMap<>();
        if (status == MessageStatus.undelivered) {
            providerError.put("severity",requestJSON.at("/event-data/severity").textValue());
            providerError.put("reason",requestJSON.at("/event-data/reason").textValue());
            providerError.put("errorCode",requestJSON.at("/event-data/delivery-status/code").asText());
            providerError.put("errorMessage",requestJSON.at("/event-data/delivery-status/message").textValue());
            providerError.put("errorDescription",requestJSON.at("/event-data/delivery-status/description").textValue());
        } else if (status == MessageStatus.failed) {
            providerError.put("reason",requestJSON.at("/event-data/reject/reason").textValue());
            providerError.put("errorDescription",requestJSON.at("/event-data/reject/description").textValue());
        }
        return providerError;
    }

    private MessageStatus mapMailgunStatustoMessageStatus(String rawMailgunStatus) {
        return switch (rawMailgunStatus) {
            case "accepted" -> MessageStatus.queued;
            case "rejected" -> MessageStatus.failed;
            case "delivered" -> MessageStatus.delivered;
            case "failed" -> MessageStatus.undelivered;
            default -> MessageStatus.unmapped;
        };
    }
}
