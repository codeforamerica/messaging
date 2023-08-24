package org.codeforamerica.messaging.providers.mailgun;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
public class MailgunSignatureVerificationService {
    @Value("${mailgun.webhook.signing.key}")
    private String mailgunWebhookSigningKey;

    Mac mac;

    @PostConstruct
    public void initializeMac() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(mailgunWebhookSigningKey.getBytes(), "HmacSHA256");
        try {
            this.mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            log.error("Error initializing MailgunSignatureVerificationService", ex);
        }
    }

    public boolean verifySignature(JsonNode requestJSON) {
        String timeStamp = requestJSON.at("/signature/timestamp").textValue();
        String token = requestJSON.at("/signature/token").textValue();
        String providedSignature = requestJSON.at("/signature/signature").textValue();

        try {
            Mac clonedMac = (Mac) this.mac.clone(); // Clone as javax.crypto is not thread safe
            byte[] hmacSha256 = clonedMac.doFinal((timeStamp+token).getBytes());
            String hmacSha256InHex = String.format("%064x", new BigInteger(1, hmacSha256));
            log.info("timeStamp: {}, token: {}, providedSignature: {}, computedSignature: {}", timeStamp, token, providedSignature, hmacSha256InHex);
            return hmacSha256InHex.equals(providedSignature);
        } catch (CloneNotSupportedException e) {
            log.error("Error cloning Mac", e);
        }
        return false;
    }

}
