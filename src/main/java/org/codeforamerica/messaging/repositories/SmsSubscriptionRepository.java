package org.codeforamerica.messaging.repositories;

import org.codeforamerica.messaging.models.PhoneNumber;
import org.codeforamerica.messaging.models.SmsSubscription;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsSubscriptionRepository extends CrudRepository<SmsSubscription, Long>  {
    public SmsSubscription findFirstByPhoneNumberOrderByCreationTimestampDesc(PhoneNumber toPhone);
}
