package org.codeforamerica.messaging.repositories;

import org.codeforamerica.messaging.models.EmailSubscription;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailSubscriptionRepository extends CrudRepository<EmailSubscription, Long>  {
    public EmailSubscription findFirstByEmailOrderByCreationTimestampDesc(String email);
}
