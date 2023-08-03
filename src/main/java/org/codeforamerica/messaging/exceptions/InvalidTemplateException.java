package org.codeforamerica.messaging.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Not a valid handlebars template")
public class InvalidTemplateException extends RuntimeException {

    public InvalidTemplateException(String message, Exception e) {
        super(message, e);
    }

}
