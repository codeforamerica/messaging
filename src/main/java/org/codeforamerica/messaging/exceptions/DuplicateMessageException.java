package org.codeforamerica.messaging.exceptions;

public class DuplicateMessageException extends MessageSendException {
    public DuplicateMessageException() {
        super("Duplicate message");
    }
}
