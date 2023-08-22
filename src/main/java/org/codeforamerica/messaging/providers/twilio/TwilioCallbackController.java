package org.codeforamerica.messaging.providers.twilio;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.jobs.SmsMessageStatusUpdateJobRequest;
import org.codeforamerica.messaging.models.Message;
import org.codeforamerica.messaging.models.MessageStatus;
import org.codeforamerica.messaging.models.PhoneNumber;
import org.codeforamerica.messaging.services.SmsService;
import org.jobrunr.scheduling.JobRequestScheduler;
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

    private final SmsService smsService;
    private final TwilioSignatureVerificationService twilioSignatureVerificationService;
    private final JobRequestScheduler jobRequestScheduler;


    public TwilioCallbackController(SmsService smsService,
            TwilioSignatureVerificationService twilioSignatureVerificationService,
            JobRequestScheduler jobRequestScheduler) {
        this.twilioSignatureVerificationService = twilioSignatureVerificationService;
        this.smsService = smsService;
        this.jobRequestScheduler = jobRequestScheduler;
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
            enqueueStatusUpdate(request, providerMessageId, rawMessageStatus);
        }
        return ResponseEntity.ok().build();
    }

    private void enqueueStatusUpdate(HttpServletRequest request, String providerMessageId,
            String rawMessageStatus) {
        MessageStatus newSmsStatus = mapTwilioStatusToMessageStatus(rawMessageStatus);
        String fromPhone = request.getParameter("From");
        Map<String, String> providerError = newSmsStatus.hadError() ? buildProviderError(request) : null;
        jobRequestScheduler.enqueue(
                new SmsMessageStatusUpdateJobRequest(providerMessageId, rawMessageStatus, newSmsStatus,
                        PhoneNumber.valueOf(fromPhone),
                        providerError));
    }

    @PostMapping(path = "/inbound", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Object> handleInbound(HttpServletRequest request) {
        String providerMessageId = request.getParameter("MessageSid");
        if (!twilioSignatureVerificationService.verifySignature(request)) {
            log.error("Signature verification failed. Provider message id: {}", providerMessageId);
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }
        String keyword = request.getParameter("OptOutType");
        PhoneNumber fromPhone = PhoneNumber.valueOf(request.getParameter("From"));
        if (keyword != null) {
            switch (keyword) {
                case "START" -> smsService.subscribe(fromPhone);
                case "STOP" -> smsService.unsubscribe(fromPhone);
                case "HELP" -> log.info("Help sought");
                default -> log.info("Unexpected keyword");
            }
        } else {
            log.info("Regular inbound message");
        }
        return ResponseEntity.ok().build();
    }

    private boolean ignorable(String rawMessageStatus) {
        return rawMessageStatus.equals("sending") || rawMessageStatus.equals("sent");
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
