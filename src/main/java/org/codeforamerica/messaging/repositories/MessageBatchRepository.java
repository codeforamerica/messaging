package org.codeforamerica.messaging.repositories;

import org.codeforamerica.messaging.models.MessageBatch;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface MessageBatchRepository extends CrudRepository<MessageBatch, Long> {
    @Query(value = "select mb from MessageBatch mb left join fetch mb.messages where mb.id = ?1")
    MessageBatch findByIdAndLoadMessages(Long id);
}
