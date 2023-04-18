package org.codeforamerica.messaging.controllers;

import jakarta.validation.Valid;
import org.codeforamerica.messaging.models.Message;
import org.codeforamerica.messaging.models.MessageRequest;
import org.codeforamerica.messaging.services.SmsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(("/api/v1/messages"))
public class MessageController {

    private final SmsService smsService;

    public MessageController(SmsService smsService) {
        this.smsService = smsService;
    }

    @PostMapping
    public ResponseEntity<String> createMessage(@Valid @RequestBody MessageRequest messageRequest) {
        Message sentMessage = this.smsService.sendSmsMessage(messageRequest.getTo(), messageRequest.getBody());
        return ResponseEntity.ok("Sent " + sentMessage);

    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Message>> getMessage(@PathVariable Long id) {
        Optional<Message> message = this.smsService.getMessage(id);
        if (message.isPresent()) {
            return ResponseEntity.ok(message);
        }
        return ResponseEntity.notFound().build();
    }
}
