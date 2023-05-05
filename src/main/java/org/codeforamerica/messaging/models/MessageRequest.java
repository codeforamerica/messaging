package org.codeforamerica.messaging.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.codeforamerica.messaging.utils.RegexPatternStrings;
import org.codeforamerica.messaging.validators.ValidMessageRequest;

import java.util.Map;

@Value
@AllArgsConstructor
@Builder
@ValidMessageRequest
public class MessageRequest {
    @Pattern(regexp = RegexPatternStrings.PHONE_NUMBER_REGEX)
    String toPhone;
    @Email
    String toEmail;
    String body;
    String subject;
    @Builder.Default
    String templateName = "default";
    @Transient
    @JsonDeserialize
    Map<String, Object> templateParams;
}
