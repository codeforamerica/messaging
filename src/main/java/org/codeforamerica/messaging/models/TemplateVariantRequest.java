package org.codeforamerica.messaging.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.codeforamerica.messaging.validators.ValidMessageContents;

@Value
@AllArgsConstructor
@Builder
@ValidMessageContents
public class TemplateVariantRequest {
    String subject;
    String emailBody;
    String smsBody;
}
