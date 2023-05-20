package org.codeforamerica.messaging.jobs;

import lombok.NoArgsConstructor;
import org.jobrunr.jobs.lambdas.JobRequest;

@NoArgsConstructor
public class SendMessageBatchJobRequest implements JobRequest {
    private Long messageBatchId;

    public SendMessageBatchJobRequest(Long messageBatchId) {
        this.messageBatchId = messageBatchId;
    }

    public Long getMessageBatchId() {
        return messageBatchId;
    }

    @Override
    public Class<SendMessageBatchJobRequestHandler> getJobRequestHandler() {
        return SendMessageBatchJobRequestHandler.class;
    }

    @Override
    public String toString() {
        return String.format("SendMessageBatchJobRequest{messageBatchId=%s}", messageBatchId);
    }
}
