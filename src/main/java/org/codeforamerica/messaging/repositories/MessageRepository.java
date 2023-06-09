package org.codeforamerica.messaging.repositories;

import org.codeforamerica.messaging.models.Message;
import org.codeforamerica.messaging.models.MessageBatchMetrics;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface MessageRepository extends CrudRepository<Message, Long> {
    Collection<Message> findMessagesByMessageBatchId(long messageBatchId);

    @Query("""
            SELECT
             SUM(CASE email.status WHEN 'accepted' THEN 1 ELSE 0 END) AS acceptedEmailCount,
             SUM(CASE email.status WHEN 'rejected' THEN 1 ELSE 0 END) AS rejectedEmailCount,
             SUM(CASE email.status WHEN 'delivered' THEN 1 ELSE 0 END) AS deliveredEmailCount,
             SUM(CASE email.status WHEN 'failed' THEN 1 ELSE 0 END) AS undeliveredEmailCount,
             SUM(CASE sms.status WHEN 'accepted' THEN 1 ELSE 0 END) AS acceptedSmsCount,
             SUM(CASE sms.status WHEN 'rejected' THEN 1 ELSE 0 END) AS rejectedSmsCount,
             SUM(CASE sms.status WHEN 'delivered' THEN 1 ELSE 0 END) AS deliveredSmsCount,
             SUM(CASE sms.status WHEN 'undelivered' THEN 1 ELSE 0 END) AS undeliveredSmsCount
             FROM Message msg
             LEFT JOIN msg.emailMessage email
             LEFT JOIN msg.smsMessage sms
             WHERE msg.messageBatch.id = ?1
             """)
    MessageBatchMetrics getMetrics(Long messageBatchId);
}
