package org.codeforamerica.messaging.providers.mailgun;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import com.mailgun.model.message.MessageResponse;
import lombok.extern.slf4j.Slf4j;
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

    public EmailMessage sendMessage(String toEmail, String body, String subject) {
        MailgunMessagesApi mailgunMessagesApi = MailgunClient.config(mailgunApiKey)
                .createApi(MailgunMessagesApi.class);

        com.mailgun.model.message.Message mailgunMessage =
                com.mailgun.model.message.Message.builder()
                        .from(from)
                        .to(toEmail)
                        .subject(subject)
                        .text(body)
                        .build();

        MessageResponse response = mailgunMessagesApi.sendMessage(mailgunDomain, mailgunMessage);

        return EmailMessage.builder()
                .fromEmail(from)
                .toEmail(toEmail)
                .subject(subject)
                .body(body)
                .providerMessageId(cleanupProviderId(response.getId()))
                .status("accepted")
                .providerCreatedAt(OffsetDateTime.now())
                .build();
    }

    private String cleanupProviderId(String providerId) {
        return providerId.replace("<", "").replace(">", "");
    }

}

