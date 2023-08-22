package org.codeforamerica.messaging.repositories;

import org.codeforamerica.messaging.models.Message;
import org.codeforamerica.messaging.models.MessageBatchMetrics;
import org.codeforamerica.messaging.models.PhoneNumber;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

public interface MessageRepository extends CrudRepository<Message, Long> {
    Collection<Message> findMessagesByMessageBatchId(long messageBatchId);
    long countByTemplateVariantId(long templateVariantId);

    @Query("""
            SELECT
             SUM(CASE WHEN emailStatus = 'submission_succeeded' OR emailStatus = 'queued' OR emailStatus = 'sent' THEN 1 ELSE 0 END) AS queuedEmailCount,
             SUM(CASE WHEN emailStatus = 'submission_failed' OR emailStatus = 'failed' THEN 1 ELSE 0 END) AS failedEmailCount,
             SUM(CASE WHEN emailStatus = 'delivered' THEN 1 ELSE 0 END) AS deliveredEmailCount,
             SUM(CASE WHEN emailStatus = 'undelivered' THEN 1 ELSE 0 END) AS undeliveredEmailCount,
             SUM(CASE WHEN emailStatus = 'unsubscribed' THEN 1 ELSE 0 END) AS unsubscribedEmailCount,
             SUM(CASE WHEN emailStatus = 'unmapped' THEN 1 ELSE 0 END) AS unmappedEmailCount,
             SUM(CASE WHEN smsStatus = 'submission_succeeded' OR smsStatus = 'queued' OR smsStatus = 'sent' THEN 1 ELSE 0 END) AS queuedSmsCount,
             SUM(CASE WHEN smsStatus = 'submission_failed' OR smsStatus = 'failed' THEN 1 ELSE 0 END) AS failedSmsCount,
             SUM(CASE WHEN smsStatus = 'delivered' THEN 1 ELSE 0 END) AS deliveredSmsCount,
             SUM(CASE WHEN smsStatus = 'undelivered' THEN 1 ELSE 0 END) AS undeliveredSmsCount,
             SUM(CASE WHEN smsStatus = 'unsubscribed' THEN 1 ELSE 0 END) AS unsubscribedSmsCount,
             SUM(CASE WHEN smsStatus = 'unmapped' THEN 1 ELSE 0 END) AS unmappedSmsCount
             FROM Message msg
            WHERE msg.messageBatch.id = ?1
             """)
    MessageBatchMetrics getMetrics(Long messageBatchId);

    @Query("""
           SELECT msg FROM Message msg WHERE
            ((:toEmail IS NULL OR msg.toEmail = :toEmail) OR (:toPhone IS NULL OR msg.toPhone = :toPhone))
            AND msg.templateVariant.template.name = :templateName
            AND msg.updateTimestamp > :updateTimestamp
    """)
    List<Message> findRecipientMessagesWithSameTemplateUpdatedRecently(
        @Param("toEmail") String toEmail,
        @Param("toPhone") PhoneNumber toPhone,
        @Param("templateName") String templateName,
        @Param("updateTimestamp") OffsetDateTime updateTimestamp);
}
