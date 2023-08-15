package org.codeforamerica.messaging.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Set;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class MissingHeadersException extends RuntimeException {

    public MissingHeadersException(String headerDetails, Set<String> missingHeaders) {
        super("Batch input is missing data, %s: %s".formatted(headerDetails, missingHeaders));
    }
}
