package org.codeforamerica.messaging.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "At least one template variant is required")
public class EmptyTemplateVariantsException extends RuntimeException {

    public EmptyTemplateVariantsException(String message) {
        super(message);
    }

}
