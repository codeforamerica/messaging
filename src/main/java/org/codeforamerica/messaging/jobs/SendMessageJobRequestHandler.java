package org.codeforamerica.messaging.jobs;

import org.codeforamerica.messaging.services.MessageService;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.lambdas.JobRequestHandler;
import org.springframework.stereotype.Component;

@Component
public class SendMessageJobRequestHandler implements JobRequestHandler<SendMessageJobRequest> {
    private final MessageService messageService;

    public SendMessageJobRequestHandler(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    @Job(name="SendMessage %0")
    public void run(SendMessageJobRequest sendMessageJobRequest) throws Exception {
        messageService.sendMessage(sendMessageJobRequest.getMessageId());
    }
}
