package org.codeforamerica.messaging.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Set;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class MissingTemplateHeadersException extends MissingHeadersException {

    public MissingTemplateHeadersException(Set<String> missingHeaders) {
        super("template parameters not found", missingHeaders);
    }
}
