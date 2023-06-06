package org.codeforamerica.messaging.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.codeforamerica.messaging.models.Message;
import org.codeforamerica.messaging.models.MessageBatch;
import org.codeforamerica.messaging.models.MessageBatchRequest;
import org.codeforamerica.messaging.models.MessageRequest;
import org.codeforamerica.messaging.services.MessageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping(("/api/v1"))
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping(path="/messages")
    @Operation(summary = "Send a message request")
    public ResponseEntity<Message> createMessage(@Valid @RequestBody MessageRequest messageRequest) {
        Message sentMessage = messageService.scheduleMessage(messageRequest);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path(
                "/{id}").buildAndExpand(sentMessage.getId()).toUri();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location);
        return new ResponseEntity<>(sentMessage, responseHeaders, HttpStatus.CREATED);
    }

    @PostMapping(path="/message_batches", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @Operation(summary = "Send a message batch request")
    public ResponseEntity<MessageBatch> createMessageBatch(@Valid @ModelAttribute MessageBatchRequest messageBatchRequest) throws IOException {
        MessageBatch messageBatch = messageService.enqueueMessageBatch(messageBatchRequest);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path(
                "/{id}").buildAndExpand(messageBatch.getId()).toUri();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location);
        return new ResponseEntity<>(messageBatch, responseHeaders, HttpStatus.CREATED);
    }

    @GetMapping("/messages/{id}")
    @Operation(summary = "Get a message status")
    public ResponseEntity<Optional<Message>> getMessage(@PathVariable Long id) {
        Optional<Message> message = messageService.getMessage(id);
        if (message.isPresent()) {
            return ResponseEntity.ok(message);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(path="/message_batches/{id}")
    public ResponseEntity<Optional<MessageBatch>> getMessageBatch(@PathVariable Long id) {
        Optional<MessageBatch> messageBatch = messageService.getMessageBatch(id);
        if (messageBatch.isPresent()) {
            return ResponseEntity.ok(messageBatch);
        }
        return ResponseEntity.notFound().build();
    }
}
