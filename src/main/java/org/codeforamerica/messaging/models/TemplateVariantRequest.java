package org.codeforamerica.messaging.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.codeforamerica.messaging.validators.ValidMessageable;

@Value
@AllArgsConstructor
@Builder
@ValidMessageable
public class TemplateVariantRequest implements Messageable {
    String subject;
    String emailBody;
    String smsBody;
}
