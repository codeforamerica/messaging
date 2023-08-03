package org.codeforamerica.messaging.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "At least one template variant is currently in use and cannot be deleted")
public class TemplateInUseException extends RuntimeException {

    public TemplateInUseException(String message) {
        super(message);
    }

}
