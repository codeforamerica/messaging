package org.codeforamerica.messaging.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class RecipientsFileMissingHeadersException extends RuntimeException {

    public RecipientsFileMissingHeadersException(String message) {
        super(message);
    }
}
