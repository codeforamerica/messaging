package org.codeforamerica.messaging.controllers;

import jakarta.validation.Valid;
import org.codeforamerica.messaging.models.Message;
import org.codeforamerica.messaging.services.MessageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping(("/api/v1/messages"))
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<Message> createMessage(@Valid @RequestBody Message message) {
        Message sentMessage = messageService.sendMessage(message);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path(
                "/{id}").buildAndExpand(sentMessage.getId()).toUri();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location);
        return new ResponseEntity<>(sentMessage, responseHeaders, HttpStatus.CREATED);

    }


    @GetMapping("/{id}")
    public ResponseEntity<Optional<Message>> getMessage(@PathVariable Long id) {
        Optional<Message> message = messageService.getMessage(id);
        if (message.isPresent()) {
            return ResponseEntity.ok(message);
        }
        return ResponseEntity.notFound().build();
    }

}
