package org.codeforamerica.messaging.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import org.codeforamerica.messaging.models.MessageRequest;

public class MessageRequestValidator implements ConstraintValidator<ValidMessageRequest, MessageRequest> {

    @Override
    public boolean isValid(MessageRequest messageRequest, ConstraintValidatorContext constraintValidatorContext) {
        return StringUtils.isNotBlank(messageRequest.getToPhone()) || StringUtils.isNotBlank(messageRequest.getToEmail());
    }
}
