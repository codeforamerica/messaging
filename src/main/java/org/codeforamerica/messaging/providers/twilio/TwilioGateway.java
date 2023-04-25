package org.codeforamerica.messaging.providers.twilio;

import com.twilio.Twilio;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.models.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

@Service
@Slf4j
public class TwilioGateway {

    public static final String DEFAULT_NUMBER = "0000000000";
    @Value("${twilio.account.sid}")
    private String twilioAccountSid;
    @Value("${twilio.auth.token}")
    private String twilioAuthToken;
    @Value("${twilio.messaging.service.sid}")
    private String twilioMessagingServiceSid;

    private static OffsetDateTime toOffsetDateTime(ZonedDateTime zonedDateTime) {
        return zonedDateTime == null ? null : zonedDateTime.toOffsetDateTime();
    }

    public Message sendMessage(String to, String body) {
        Twilio.init(twilioAccountSid, twilioAuthToken);
        com.twilio.rest.api.v2010.account.Message twilioMessage =
                com.twilio.rest.api.v2010.account.Message.creator(
                                new com.twilio.type.PhoneNumber(to),
                                twilioMessagingServiceSid,
                                body)
                        .create();

        return Message.builder()
                .fromNumber(DEFAULT_NUMBER)
                .toNumber(twilioMessage.getTo())
                .body(twilioMessage.getBody())
                .providerMessageId(twilioMessage.getSid())
                .status(String.valueOf(twilioMessage.getStatus()))
                .providerCreatedAt(toOffsetDateTime(twilioMessage.getDateCreated()))
                .build();
    }

}

