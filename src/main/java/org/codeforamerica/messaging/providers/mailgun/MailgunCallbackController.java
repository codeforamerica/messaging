package org.codeforamerica.messaging.providers.mailgun;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.models.EmailMessage;
import org.codeforamerica.messaging.models.EmailSubscription;
import org.codeforamerica.messaging.repositories.EmailMessageRepository;
import org.codeforamerica.messaging.repositories.EmailSubscriptionRepository;
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
    private final EmailMessageRepository emailMessageRepository;
    private final EmailSubscriptionRepository emailSubscriptionRepository;
    private final MailgunSignatureVerificationService mailgunSignatureVerificationService;

    public MailgunCallbackController(EmailMessageRepository emailMessageRepository,
                                     EmailSubscriptionRepository emailSubscriptionRepository,
                                     MailgunSignatureVerificationService mailgunSignatureVerificationService) {
        this.emailMessageRepository = emailMessageRepository;
        this.emailSubscriptionRepository = emailSubscriptionRepository;
        this.mailgunSignatureVerificationService = mailgunSignatureVerificationService;
    }

    @PostMapping(path = "/status")
    public ResponseEntity<Object> updateStatus(@RequestBody JsonNode requestJSON) {
        if (!mailgunSignatureVerificationService.verifySignature(requestJSON)) {
            log.error("Signature verification failed");
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        EmailMessage emailMessage = emailMessageRepository.findFirstByProviderMessageId(
                requestJSON.at("/event-data/message/headers/message-id").textValue());
        String status = requestJSON.at("/event-data/event").textValue();
        emailMessage.setStatus(status);
        if (hadError(status)) {
            emailMessage.setProviderError(buildProviderError(requestJSON, status));
        } else if (status.equals("unsubscribed")) {
            unsubscribeEmail(requestJSON);
        }

        emailMessageRepository.save(emailMessage);

        return ResponseEntity.ok().build();
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

    private static boolean hadError(String status) {
        return status.equals("failed") || status.equals("rejected");
    }

    private static Map<String, String> buildProviderError(JsonNode requestJSON, String status) {
        log.info(requestJSON.toString());
        Map<String, String> providerError = new HashMap<>();
        if (status.equals("failed")) {
            providerError.put("severity",requestJSON.at("/event-data/severity").textValue());
            providerError.put("reason",requestJSON.at("/event-data/reason").textValue());
            providerError.put("errorCode",requestJSON.at("/event-data/delivery-status/code").asText());
            providerError.put("errorMessage",requestJSON.at("/event-data/delivery-status/message").textValue());
            providerError.put("errorDescription",requestJSON.at("/event-data/delivery-status/description").textValue());
        } else if (status.equals("rejected")) {
            providerError.put("reason",requestJSON.at("/event-data/reject/reason").textValue());
            providerError.put("errorDescription",requestJSON.at("/event-data/reject/description").textValue());
        }
        return providerError;
    }
}
