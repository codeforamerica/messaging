package org.codeforamerica.messaging.exceptions;

public class UnsubscribedException extends MessageSendException {
    public UnsubscribedException() {
        super("Unsubscribed");
    }
}
