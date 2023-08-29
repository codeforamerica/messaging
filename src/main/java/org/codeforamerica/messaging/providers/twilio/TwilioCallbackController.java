package org.codeforamerica.messaging.providers.twilio;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.models.MessageStatus;
import org.codeforamerica.messaging.models.SmsMessage;
import org.codeforamerica.messaging.repositories.SmsMessageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/public/twilio_callbacks")
@Slf4j
public class TwilioCallbackController {

    private final SmsMessageRepository smsMessageRepository;
    private final TwilioSignatureVerificationService twilioSignatureVerificationService;

    public TwilioCallbackController(SmsMessageRepository smsMessageRepository,
            TwilioSignatureVerificationService twilioSignatureVerificationService) {
        this.smsMessageRepository = smsMessageRepository;
        this.twilioSignatureVerificationService = twilioSignatureVerificationService;
    }

    @PostMapping(path = "/status", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Object> updateStatus(HttpServletRequest request) {
        String providerMessageId = request.getParameter("MessageSid");
        if (!twilioSignatureVerificationService.verifySignature(request)) {
            log.error("Signature verification failed. Provider message id: {}", providerMessageId);
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }
        String rawMessageStatus = request.getParameter("MessageStatus");
        if (!ignorable(rawMessageStatus)) {
            scheduleStatusUpdate(request, providerMessageId, rawMessageStatus);
        }
        return ResponseEntity.ok().build();
    }

    private void scheduleStatusUpdate(HttpServletRequest request, String providerMessageId,
            String rawMessageStatus) {
        MessageStatus newSmsStatus = mapTwilioStatusToMessageStatus(rawMessageStatus);
        String fromPhone = request.getParameter("From");
        Map<String, String> providerError = hadError(newSmsStatus) ? buildProviderError(request) : null;
        executeStatusUpdate(providerMessageId, fromPhone, rawMessageStatus, newSmsStatus, providerError);
    }

    private void executeStatusUpdate(String providerMessageId, String fromPhone, String rawSmsStatus, MessageStatus newSmsStatus, Map<String, String> providerError) {
        SmsMessage smsMessage = smsMessageRepository.findFirstByProviderMessageId(providerMessageId);
        MessageStatus currentSmsStatus = smsMessage.getMessage().getSmsStatus();
        if (newSmsStatus.isAfter(currentSmsStatus)) {
            log.info("Updating status. Provider message id: {}, current status: {}, new status: {}", providerMessageId, currentSmsStatus, newSmsStatus);
            smsMessage.getMessage().setRawSmsStatus(rawSmsStatus);
            smsMessage.getMessage().setSmsStatus(newSmsStatus);
            smsMessage.setFromPhone(fromPhone);
            smsMessage.setProviderError(providerError);
            smsMessageRepository.save(smsMessage);
        } else {
            log.info("Ignoring earlier status {}, current status: {}", newSmsStatus, currentSmsStatus);
        }
    }

    private boolean ignorable(String rawMessageStatus) {
        return rawMessageStatus.equals("sending") || rawMessageStatus.equals("sent");
    }

    private static boolean hadError(MessageStatus status) {
        return status == MessageStatus.failed || status == MessageStatus.undelivered;
    }

    private static Map<String, String> buildProviderError(HttpServletRequest request) {
        Map<String, String> providerError = new HashMap<>();
        providerError.put("errorCode", request.getParameter("ErrorCode"));
        if (request.getParameter("ErrorMessage") != null) {
            providerError.put("errorMessage", request.getParameter("ErrorMessage"));
        }
        return providerError;
    }

    private MessageStatus mapTwilioStatusToMessageStatus(String rawTwilioStatus) {
        return switch (rawTwilioStatus) {
            case "queued", "accepted" -> MessageStatus.queued;
            case "sent" -> MessageStatus.sent;
            case "failed" -> MessageStatus.failed;
            case "delivered" -> MessageStatus.delivered;
            case "undelivered" -> MessageStatus.undelivered;
            default -> MessageStatus.unmapped;
        };
    }
}
