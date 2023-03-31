package org.codeforamerica.messaging.providers.twilio;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.models.Message;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/twilio_callbacks")
@Slf4j
public class TwilioCallbackController {

    private final MessageRepository messageRepository;

    public TwilioCallbackController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @PostMapping(path = "/status", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Object> updateStatus(@RequestParam Map<String, String> twilioStatusMessage) {
        log.info("Received twilio callback: " + twilioStatusMessage);
        Message message = messageRepository.findFirstByProviderMessageId(twilioStatusMessage.get("MessageSid"));
        Message updatedMessage = message.toBuilder()
                .from(twilioStatusMessage.get("From"))
                .status(twilioStatusMessage.get("MessageStatus"))
                .build();
        messageRepository.save(updatedMessage);
        return ResponseEntity.ok().build();
    }
}
