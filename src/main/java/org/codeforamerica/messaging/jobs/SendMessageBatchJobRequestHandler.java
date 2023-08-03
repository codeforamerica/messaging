package org.codeforamerica.messaging.jobs;

import org.codeforamerica.messaging.services.MessageService;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.lambdas.JobRequestHandler;
import org.springframework.stereotype.Component;

@Component
public class SendMessageBatchJobRequestHandler implements JobRequestHandler<SendMessageBatchJobRequest> {
    private final MessageService messageService;

    public SendMessageBatchJobRequestHandler(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    @Job(name="SendMessageBatch %0")
    public void run(SendMessageBatchJobRequest sendMessageBatchJobRequest) {
        messageService.scheduleMessageBatch(sendMessageBatchJobRequest.getMessageBatchId());
    }
}
