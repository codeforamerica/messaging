package org.codeforamerica.messaging.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Template name is taken")
public class DuplicateTemplateException extends RuntimeException {

    public DuplicateTemplateException(String message) {
        super(message);
    }

}
