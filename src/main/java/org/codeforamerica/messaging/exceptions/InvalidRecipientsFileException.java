package org.codeforamerica.messaging.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidRecipientsFileException extends RuntimeException {

    public InvalidRecipientsFileException(IOException e) {
        super(e);
    }
}
