package org.codeforamerica.messaging.providers.mailgun;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.models.EmailMessage;
import org.codeforamerica.messaging.repositories.EmailMessageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/mailgun_callbacks")
@Slf4j
public class MailgunCallbackController {
    private final EmailMessageRepository emailMessageRepository;
    private final MailgunSignatureVerificationService mailgunSignatureVerificationService;

    public MailgunCallbackController(EmailMessageRepository emailMessageRepository,
            MailgunSignatureVerificationService mailgunSignatureVerificationService) {
        this.emailMessageRepository = emailMessageRepository;
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
        emailMessage.setStatus(requestJSON.at("/event-data/event").textValue());
        emailMessageRepository.save(emailMessage);

        return ResponseEntity.ok().build();
    }
}
