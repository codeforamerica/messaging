package org.codeforamerica.messaging.models;

public interface MessageBatchMetrics {
    Integer getAcceptedEmailCount();
    Integer getRejectedEmailCount();
    Integer getDeliveredEmailCount();
    Integer getUndeliveredEmailCount();
    Integer getAcceptedSmsCount();
    Integer getRejectedSmsCount();
    Integer getDeliveredSmsCount();
    Integer getUndeliveredSmsCount();
}
