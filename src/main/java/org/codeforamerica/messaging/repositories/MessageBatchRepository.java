package org.codeforamerica.messaging.repositories;

import org.codeforamerica.messaging.models.MessageBatch;
import org.springframework.data.repository.CrudRepository;

import java.time.OffsetDateTime;

public interface MessageBatchRepository extends CrudRepository<MessageBatch, Long> {
    int countByTemplateIdAndSendAtIsAfter(Long templateId, OffsetDateTime offsetDateTime);
}
