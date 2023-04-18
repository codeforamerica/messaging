package org.codeforamerica.messaging.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class MessageRequest {

    @NotBlank
    @Pattern(regexp = "\\A1?\\d{10}\\z")
    String to;
    @NotBlank
    String body;
}
