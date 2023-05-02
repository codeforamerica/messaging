package org.codeforamerica.messaging.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import org.codeforamerica.messaging.models.Message;

public class MessageValidator implements ConstraintValidator<ValidMessage, Message> {

    @Override
    public boolean isValid(Message message, ConstraintValidatorContext constraintValidatorContext) {
        if (StringUtils.isNoneBlank(message.getToEmail(), message.getSubject())) {
            return true;
        } else if (StringUtils.isAllBlank(message.getToEmail(), message.getSubject())) {
            return StringUtils.isNotBlank(message.getToPhone());
        }
        return false;
    }
}
