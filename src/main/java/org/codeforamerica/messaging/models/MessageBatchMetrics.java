package org.codeforamerica.messaging.models;

public interface MessageBatchMetrics {
    Integer getQueuedEmailCount();
    Integer getFailedEmailCount();
    Integer getDeliveredEmailCount();
    Integer getUndeliveredEmailCount();
    Integer getUnsubscribedEmailCount();
    Integer getUnmappedEmailCount();
    Integer getQueuedSmsCount();
    Integer getFailedSmsCount();
    Integer getDeliveredSmsCount();
    Integer getUndeliveredSmsCount();
    Integer getUnsubscribedSmsCount();
    Integer getUnmappedSmsCount();
}
