package org.codeforamerica.messaging.repositories;

import org.codeforamerica.messaging.models.Message;
import org.codeforamerica.messaging.models.MessageBatchMetrics;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface MessageRepository extends CrudRepository<Message, Long> {
    Collection<Message> findMessagesByMessageBatchId(long messageBatchId);
    long countByTemplateVariantId(long templateVariantId);

    @Query("""
            SELECT
             SUM(CASE emailStatus WHEN 'queued' THEN 1 ELSE 0 END) AS queuedEmailCount,
             SUM(CASE emailStatus WHEN 'failed' THEN 1 ELSE 0 END) AS failedEmailCount,
             SUM(CASE emailStatus WHEN 'delivered' THEN 1 ELSE 0 END) AS deliveredEmailCount,
             SUM(CASE emailStatus WHEN 'undelivered' THEN 1 ELSE 0 END) AS undeliveredEmailCount,
             SUM(CASE emailStatus WHEN 'unsubscribed' THEN 1 ELSE 0 END) AS unsubscribedEmailCount,
             SUM(CASE emailStatus WHEN 'unmapped' THEN 1 ELSE 0 END) AS unmappedEmailCount,
             SUM(CASE smsStatus WHEN 'queued' THEN 1 ELSE 0 END) AS queuedSmsCount,
             SUM(CASE smsStatus WHEN 'failed' THEN 1 ELSE 0 END) AS failedSmsCount,
             SUM(CASE smsStatus WHEN 'delivered' THEN 1 ELSE 0 END) AS deliveredSmsCount,
             SUM(CASE smsStatus WHEN 'undelivered' THEN 1 ELSE 0 END) AS undeliveredSmsCount,
             SUM(CASE smsStatus WHEN 'unsubscribed' THEN 1 ELSE 0 END) AS unsubscribedSmsCount,
             SUM(CASE smsStatus WHEN 'unmapped' THEN 1 ELSE 0 END) AS unmappedSmsCount
             FROM Message msg
            WHERE msg.messageBatch.id = ?1
             """)
    MessageBatchMetrics getMetrics(Long messageBatchId);
}
