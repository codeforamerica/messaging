package org.codeforamerica.messaging.repositories;

import org.codeforamerica.messaging.models.Message;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends CrudRepository<Message, Long> {

    Message findFirstByProviderMessageId(String providerMessageId);

}