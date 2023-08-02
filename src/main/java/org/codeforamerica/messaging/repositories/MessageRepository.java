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
             SUM(CASE emailStatus WHEN 'accepted' THEN 1 ELSE 0 END) AS acceptedEmailCount,
             SUM(CASE emailStatus WHEN 'rejected' THEN 1 ELSE 0 END) AS rejectedEmailCount,
             SUM(CASE emailStatus WHEN 'delivered' THEN 1 ELSE 0 END) AS deliveredEmailCount,
             SUM(CASE emailStatus WHEN 'failed' THEN 1 ELSE 0 END) AS undeliveredEmailCount,
             SUM(CASE smsStatus WHEN 'accepted' THEN 1 ELSE 0 END) AS acceptedSmsCount,
             SUM(CASE smsStatus WHEN 'rejected' THEN 1 ELSE 0 END) AS rejectedSmsCount,
             SUM(CASE smsStatus WHEN 'delivered' THEN 1 ELSE 0 END) AS deliveredSmsCount,
             SUM(CASE smsStatus WHEN 'undelivered' THEN 1 ELSE 0 END) AS undeliveredSmsCount
             FROM Message msg
            WHERE msg.messageBatch.id = ?1
             """)
    MessageBatchMetrics getMetrics(Long messageBatchId);
}
