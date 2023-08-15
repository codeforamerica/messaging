package org.codeforamerica.messaging.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Set;

import static org.codeforamerica.messaging.utils.CSVReader.EMAIL_HEADER;
import static org.codeforamerica.messaging.utils.CSVReader.PHONE_HEADER;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class MissingRecipientInfoHeadersException extends MissingHeadersException {

    public static Set<String> RECIPIENT_INFO_HEADERS = Set.of(PHONE_HEADER, EMAIL_HEADER);

    public MissingRecipientInfoHeadersException(Set<String> missingParams) {
        super("at least one of these is required", missingParams);
    }
}
