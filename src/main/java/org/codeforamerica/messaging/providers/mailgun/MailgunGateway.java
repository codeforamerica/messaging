package org.codeforamerica.messaging.providers.mailgun;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import com.mailgun.model.message.MessageResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.exceptions.MessageSendException;
import org.codeforamerica.messaging.models.EmailMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@Slf4j
public class MailgunGateway {

    @Value("${mailgun.api.key}")
    private String mailgunApiKey;
    @Value("${mailgun.api.domain}")
    private String mailgunDomain;
    @Value("${mailgun.api.from}")
    private String from;

    public EmailMessage sendMessage(String toEmail, String body, String subject) throws MessageSendException {
        MailgunMessagesApi mailgunMessagesApi = MailgunClient.config(mailgunApiKey)
                .createApi(MailgunMessagesApi.class);

        com.mailgun.model.message.Message mailgunMessage =
                com.mailgun.model.message.Message.builder()
                        .from(from)
                        .to(toEmail)
                        .subject(subject)
                        .text(body)
                        .build();

        MessageResponse response;
        try {
            response = mailgunMessagesApi.sendMessage(mailgunDomain, mailgunMessage);
        } catch (FeignException e) {
            throw new MessageSendException(e.getMessage());
        }

        return EmailMessage.builder()
                .fromEmail(from)
                .toEmail(toEmail)
                .subject(subject)
                .body(body)
                .providerMessageId(cleanupProviderId(response.getId()))
                .providerCreatedAt(OffsetDateTime.now())
                .build();
    }

    private String cleanupProviderId(String providerId) {
        return providerId.replace("<", "").replace(">", "");
    }

}
