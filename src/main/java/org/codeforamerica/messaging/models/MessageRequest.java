package org.codeforamerica.messaging.models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.codeforamerica.messaging.utils.RegexPatternStrings;
import org.codeforamerica.messaging.validators.ValidMessageRequest;

@Value
@AllArgsConstructor
@Builder
@ValidMessageRequest
public class MessageRequest {
    @Pattern(regexp = RegexPatternStrings.PHONE_NUMBER_REGEX)
    String toPhone;
    @Email
    String toEmail;
    @NotBlank
    String body;
    String subject;
}
