package org.codeforamerica.messaging.models;

public enum MessageStatus {
    duplicate,
    submission_succeeded,
    submission_failed,
    unmapped,
    unsubscribed,
    queued,
    sent,
    failed,
    delivered,
    undelivered;

    public boolean isAfter(MessageStatus other) {
        return other == null || compareTo(other) > 0;
    }

    public boolean hadError() {
        return this == MessageStatus.failed || this == MessageStatus.undelivered;
    }
}
