package org.codeforamerica.messaging.jobs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codeforamerica.messaging.models.MessageStatus;
import org.jobrunr.jobs.lambdas.JobRequest;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class SmsMessageStatusUpdateJobRequest implements JobRequest {
    @Getter
    private String providerMessageId;

    @Getter
    private String rawStatus;

    @Getter
    private MessageStatus messageStatus;

    @Getter
    private String fromPhone;

    @Getter
    private Map<String, String> providerError;

    @Override
    public Class<SmsMessageStatusUpdateJobRequestHandler> getJobRequestHandler() {
        return SmsMessageStatusUpdateJobRequestHandler.class;
    }

    @Override
    public String toString() {
        return String.format("SmsMessageStatusUpdateJobRequest{providerMessageId=%s}", providerMessageId);
    }
}
