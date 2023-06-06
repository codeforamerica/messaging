package org.codeforamerica.messaging.models;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@AllArgsConstructor
@Builder
public class TemplateVariantRequest {
    String subject;
    @NotBlank
    String body;
}
