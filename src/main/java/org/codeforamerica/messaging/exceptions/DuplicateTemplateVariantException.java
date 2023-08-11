package org.codeforamerica.messaging.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Template variant already exists")
public class DuplicateTemplateVariantException extends RuntimeException {

    public DuplicateTemplateVariantException(String message) {
        super(message);
    }

}
