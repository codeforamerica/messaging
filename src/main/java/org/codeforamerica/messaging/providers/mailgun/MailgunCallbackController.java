package org.codeforamerica.messaging.providers.mailgun;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.models.EmailMessage;
import org.codeforamerica.messaging.repositories.EmailMessageRepository;
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

    public MailgunCallbackController(EmailMessageRepository emailMessageRepository) {
        this.emailMessageRepository = emailMessageRepository;
    }

    @PostMapping(path = "/status")
    public ResponseEntity<Object> updateStatus(@RequestBody JsonNode mailgunCallback) {
        EmailMessage emailMessage = emailMessageRepository.findFirstByProviderMessageId(
                mailgunCallback.at("/event-data/message/headers/message-id").textValue());
        emailMessage.setStatus(mailgunCallback.at("/event-data/event").textValue());
        emailMessageRepository.save(emailMessage);

        return ResponseEntity.ok().build();
    }
}
