package org.codeforamerica.messaging.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.util.Set;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Error applying parameters to template")
public class ParamsInvalidException extends RuntimeException {

    public ParamsInvalidException(Set<String> templateParamKeys, IOException e) {
        super(templateParamKeys.toString(), e);
    }
}
