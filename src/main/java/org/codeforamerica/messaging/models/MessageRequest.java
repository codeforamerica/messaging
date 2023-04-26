package org.codeforamerica.messaging.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Value;
import org.codeforamerica.messaging.utils.RegexPatternStrings;

@Value
@Builder(toBuilder = true)
public class MessageRequest {

    @NotBlank
    @Pattern(regexp = RegexPatternStrings.PHONE_NUMBER_REGEX)
    String to;
    @NotBlank
    String body;
}
