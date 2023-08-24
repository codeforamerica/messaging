package org.codeforamerica.messaging.models;

public enum MessageStatus {
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
}
