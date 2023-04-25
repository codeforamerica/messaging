package org.codeforamerica.messaging.repositories;

import org.codeforamerica.messaging.models.SmsMessage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends CrudRepository<SmsMessage, Long> {

    SmsMessage findFirstByProviderMessageId(String providerMessageId);

}