package org.codeforamerica.messaging.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.codeforamerica.messaging.utils.RegexPatternStrings;
import org.codeforamerica.messaging.validators.ValidMessageRequest;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.codeforamerica.messaging.models.TemplateVariant.DEFAULT_LANGUAGE;
import static org.codeforamerica.messaging.models.TemplateVariant.DEFAULT_TREATMENT;

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
    String templateName;
    @Transient
    @JsonDeserialize
    Map<String, String> templateParams;
    @Future
    OffsetDateTime sendAt;

    public String getLanguage() {
        if (this.getTemplateParams() != null && this.getTemplateParams().get("language") != null) {
            return this.getTemplateParams().get("language");
        }
        return DEFAULT_LANGUAGE;
    }

    public String getTreatment() {
        if (this.getTemplateParams() != null && this.getTemplateParams().get("treatment") != null) {
            return this.getTemplateParams().get("treatment");
        }
        return DEFAULT_TREATMENT;
    }
}
