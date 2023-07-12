package org.codeforamerica.messaging.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import org.codeforamerica.messaging.models.Message;
import org.codeforamerica.messaging.models.TemplateVariant;
import org.codeforamerica.messaging.models.TemplateVariantRequest;

public class MessageContentsValidator implements ConstraintValidator<ValidMessageContents, Object> {
    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        if (object instanceof Message message) {
            return StringUtils.isNotBlank(message.getSmsBody()) ||
                    (StringUtils.isNotBlank(message.getEmailBody()) && StringUtils.isNotBlank(message.getSubject()));
        }
        else if (object instanceof TemplateVariant templateVariant) {
            return StringUtils.isNotBlank(templateVariant.getSmsBody()) ||
                    (StringUtils.isNotBlank(templateVariant.getEmailBody()) && StringUtils.isNotBlank(templateVariant.getSubject()));
        }
        else if (object instanceof TemplateVariantRequest templateVariantRequest) {
            return StringUtils.isNotBlank(templateVariantRequest.getSmsBody()) ||
                    (StringUtils.isNotBlank(templateVariantRequest.getEmailBody()) && StringUtils.isNotBlank(templateVariantRequest.getSubject()));
        }
        return false;
    }
}
