package org.codeforamerica.messaging.repositories;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.codeforamerica.messaging.models.EmailMessage;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface EmailMessageRepository extends CrudRepository<EmailMessage, Long> {

    EmailMessage findFirstByProviderMessageId(String providerMessageId);

    List<EmailMessage> findAllByToEmailAndUpdateTimestampAfter(
        @NotBlank @Email String toEmail,
        OffsetDateTime updateTimestamp);
}