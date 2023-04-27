package org.codeforamerica.messaging.controllers;

import jakarta.validation.Valid;
import org.codeforamerica.messaging.models.SmsMessage;
import org.codeforamerica.messaging.models.EmailMessage;
import org.codeforamerica.messaging.models.Message;
import org.codeforamerica.messaging.repositories.MessageRepository;
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
    private final MessageRepository messageRepository;

    public MessageController(SmsService smsService, EmailService emailService, MessageRepository messageRepository) {
        this.smsService = smsService;
        this.emailService = emailService;
        this.messageRepository = messageRepository;
    }

    @PostMapping
    public ResponseEntity<String> createMessage(@Valid @RequestBody Message message) {
        SmsMessage sentSmsMessage;
        EmailMessage sentEmailMessage;
        if (message.getToPhone() != null) {
            sentSmsMessage = this.smsService.sendSmsMessage(message.getToPhone(), message.getBody());
            message.setSmsMessage(sentSmsMessage);
        }
        if (message.getToEmail() != null) {
            sentEmailMessage = this.emailService.sendEmailMessage(message.getToEmail(), message.getBody(), message.getSubject());
            message.setEmailMessage(sentEmailMessage);
        }

        messageRepository.save(message);


        return ResponseEntity.ok("Sent message(s)");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Message>> getMessage(@PathVariable Long id) {
        Optional<Message> message = messageRepository.findById(id);
        if (message.isPresent()) {
            return ResponseEntity.ok(message);
        }
        return ResponseEntity.notFound().build();
    }
}
