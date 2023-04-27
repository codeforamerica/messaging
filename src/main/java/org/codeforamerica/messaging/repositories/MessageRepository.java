package org.codeforamerica.messaging.repositories;

import org.codeforamerica.messaging.models.Message;
import org.springframework.data.repository.CrudRepository;

public interface MessageRepository extends CrudRepository<Message, Long> {

}
