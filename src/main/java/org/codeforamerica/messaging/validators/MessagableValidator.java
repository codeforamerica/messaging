package org.codeforamerica.messaging.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import org.codeforamerica.messaging.models.Messageable;

public class MessagableValidator implements ConstraintValidator<ValidMessageable, Messageable> {
    @Override
    public boolean isValid(Messageable messageable, ConstraintValidatorContext constraintValidatorContext) {
        return StringUtils.isNotBlank(messageable.getSmsBody()) ||
                (StringUtils.isNotBlank(messageable.getEmailBody()) && StringUtils.isNotBlank(messageable.getSubject()));
    }
}
