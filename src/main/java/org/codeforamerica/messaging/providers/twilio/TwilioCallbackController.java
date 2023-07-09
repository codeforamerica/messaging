package org.codeforamerica.messaging.providers.twilio;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.models.SmsMessage;
import org.codeforamerica.messaging.repositories.SmsMessageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        if (!twilioSignatureVerificationService.verifySignature(request)) {
            log.error("Signature verification failed");
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }
        SmsMessage smsMessage = smsMessageRepository.findFirstByProviderMessageId(request.getParameter("MessageSid"));
        smsMessage.setStatus(request.getParameter("MessageStatus"));
        smsMessage.setFromPhone(request.getParameter("From"));
        smsMessageRepository.save(smsMessage);
        return ResponseEntity.ok().build();
    }
}
