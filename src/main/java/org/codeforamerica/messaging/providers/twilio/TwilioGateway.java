package org.codeforamerica.messaging.providers.twilio;

import com.twilio.Twilio;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.exceptions.MessageSendException;
import org.codeforamerica.messaging.models.PhoneNumber;
import org.codeforamerica.messaging.models.SmsMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

@Service
@Slf4j
public class TwilioGateway {

    public static final PhoneNumber DEFAULT_FROM_PHONE = PhoneNumber.valueOf("0000000000");
    @Value("${twilio.account.sid}")
    private String twilioAccountSid;
    @Value("${twilio.auth.token}")
    private String twilioAuthToken;
    @Value("${twilio.messaging.service.sid}")
    private String twilioMessagingServiceSid;

    private static OffsetDateTime toOffsetDateTime(ZonedDateTime zonedDateTime) {
        return zonedDateTime == null ? null : zonedDateTime.toOffsetDateTime();
    }

    public SmsMessage sendMessage(String to, String body) throws MessageSendException {
        com.twilio.rest.api.v2010.account.Message twilioMessage =
                null;
        try {
            Twilio.init(twilioAccountSid, twilioAuthToken);
            twilioMessage = com.twilio.rest.api.v2010.account.Message.creator(
                            new com.twilio.type.PhoneNumber(to),
                            twilioMessagingServiceSid,
                            body)
                    .create();
        } catch (com.twilio.exception.TwilioException e) {
            throw new MessageSendException(e.getMessage());
        }

        return SmsMessage.builder()
                .fromPhone(DEFAULT_FROM_PHONE)
                .toPhone(PhoneNumber.valueOf(twilioMessage.getTo()))
                .body(twilioMessage.getBody())
                .providerMessageId(twilioMessage.getSid())
                .providerCreatedAt(toOffsetDateTime(twilioMessage.getDateCreated()))
                .build();
    }

}
