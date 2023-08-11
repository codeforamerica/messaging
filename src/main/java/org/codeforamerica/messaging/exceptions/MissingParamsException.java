package org.codeforamerica.messaging.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Set;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class MissingParamsException extends RuntimeException {

    public MissingParamsException(Set<String> missingParams) {
        super("Missing template parameters: %s".formatted(missingParams));
    }

}
