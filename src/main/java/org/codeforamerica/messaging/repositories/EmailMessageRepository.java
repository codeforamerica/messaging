package org.codeforamerica.messaging.repositories;

import org.codeforamerica.messaging.models.EmailMessage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailMessageRepository extends CrudRepository<EmailMessage, Long> {

    EmailMessage findFirstByProviderMessageId(String providerMessageId);

}