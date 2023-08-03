package org.codeforamerica.messaging.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Set;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ParamsMissingException extends RuntimeException {

    public ParamsMissingException(Set<String> missingParams) {
        super("Missing template parameters: %s".formatted(missingParams));
    }

}
