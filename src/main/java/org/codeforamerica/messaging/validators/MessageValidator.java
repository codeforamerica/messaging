package org.codeforamerica.messaging.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import org.codeforamerica.messaging.models.Message;

public class MessageValidator implements ConstraintValidator<ValidMessage, Message> {

    @Override
    public boolean isValid(Message message, ConstraintValidatorContext constraintValidatorContext) {
        if (StringUtils.isNotBlank(message.getToPhone())) {
            return true;
        } else
            return StringUtils.isNoneBlank(message.getToEmail(), message.getSubject());
    }
}
