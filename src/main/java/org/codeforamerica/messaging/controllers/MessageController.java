package org.codeforamerica.messaging.controllers;

import jakarta.validation.Valid;
import org.codeforamerica.messaging.models.SmsMessage;
import org.codeforamerica.messaging.models.EmailMessage;
import org.codeforamerica.messaging.models.MessageRequest;
import org.codeforamerica.messaging.services.EmailService;
import org.codeforamerica.messaging.services.SmsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(("/api/v1/messages"))
public class MessageController {

    private final SmsService smsService;
    private final EmailService emailService;

    public MessageController(SmsService smsService, EmailService emailService) {
        this.smsService = smsService;
        this.emailService = emailService;
    }

    @PostMapping
    public ResponseEntity<String> createMessage(@Valid @RequestBody MessageRequest messageRequest) {
        SmsMessage sentSmsMessage;
        EmailMessage sentEmailMessage;
        if (messageRequest.getToPhone() != null) {
            sentSmsMessage = this.smsService.sendSmsMessage(messageRequest.getToPhone(), messageRequest.getBody());
        }
        if (messageRequest.getToEmail() != null) {
            sentEmailMessage = this.emailService.sendEmailMessage(messageRequest.getToEmail(), messageRequest.getBody(), messageRequest.getSubject());
        }

        return ResponseEntity.ok("Sent message(s)");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<SmsMessage>> getMessage(@PathVariable Long id) {
        Optional<SmsMessage> message = this.smsService.getMessage(id);
        if (message.isPresent()) {
            return ResponseEntity.ok(message);
        }
        return ResponseEntity.notFound().build();
    }
}
