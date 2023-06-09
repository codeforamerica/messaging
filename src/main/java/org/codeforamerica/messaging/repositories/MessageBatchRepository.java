package org.codeforamerica.messaging.repositories;

import org.codeforamerica.messaging.models.MessageBatch;
import org.springframework.data.repository.CrudRepository;

public interface MessageBatchRepository extends CrudRepository<MessageBatch, Long> {
}
