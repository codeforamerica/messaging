package org.codeforamerica.messaging.jobs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codeforamerica.messaging.models.MessageStatus;
import org.jobrunr.jobs.lambdas.JobRequest;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class EmailMessageStatusUpdateJobRequest implements JobRequest {
    @Getter
    private String providerMessageId;

    @Getter
    private String rawStatus;

    @Getter
    private MessageStatus messageStatus;

    @Getter
    private Map<String, String> providerError;

    @Override
    public Class<EmailMessageStatusUpdateJobRequestHandler> getJobRequestHandler() {
        return EmailMessageStatusUpdateJobRequestHandler.class;
    }

    @Override
    public String toString() {
        return String.format("EmailMessageStatusUpdateJobRequest{providerMessageId=%s}", providerMessageId);
    }
}
