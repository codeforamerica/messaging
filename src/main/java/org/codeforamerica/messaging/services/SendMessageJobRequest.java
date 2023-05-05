package org.codeforamerica.messaging.services;

import lombok.NoArgsConstructor;
import org.jobrunr.jobs.lambdas.JobRequest;

@NoArgsConstructor
public class SendMessageJobRequest implements JobRequest {
    private Long messageId;

    public SendMessageJobRequest(Long messageId) {
        this.messageId = messageId;
    }

    public Long getMessageId() {
        return messageId;
    }

    @Override
    public Class<SendMessageJobRequestHandler> getJobRequestHandler() {
        return SendMessageJobRequestHandler.class;
    }

    @Override
    public String toString() {
        return "SendMessageJobRequest{" +
                "messageId=" + messageId +
                '}';
    }
}
