package org.codeforamerica.messaging.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.util.Set;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidParamsException extends RuntimeException {

    public InvalidParamsException(Set<String> templateParamKeys, IOException e) {
        super(templateParamKeys.toString(), e);
    }
}
