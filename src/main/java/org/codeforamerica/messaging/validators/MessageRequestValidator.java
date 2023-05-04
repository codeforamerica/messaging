package org.codeforamerica.messaging.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import org.codeforamerica.messaging.models.MessageRequest;

public class MessageRequestValidator implements ConstraintValidator<ValidMessageRequest, MessageRequest> {

    @Override
    public boolean isValid(MessageRequest messageRequest, ConstraintValidatorContext constraintValidatorContext) {
        if (StringUtils.isNoneBlank(messageRequest.getToEmail(), messageRequest.getSubject())) {
            return true;
        } else if (StringUtils.isAllBlank(messageRequest.getToEmail(), messageRequest.getSubject())) {
            return StringUtils.isNotBlank(messageRequest.getToPhone());
        }
        return false;
    }
}
