package org.codeforamerica.messaging.jobs;

import org.codeforamerica.messaging.services.EmailService;
import org.codeforamerica.messaging.services.SmsService;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.lambdas.JobRequestHandler;
import org.springframework.stereotype.Component;

@Component
public class EmailMessageStatusUpdateJobRequestHandler implements JobRequestHandler<EmailMessageStatusUpdateJobRequest>  {

    private final EmailService emailService;

    public EmailMessageStatusUpdateJobRequestHandler(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    @Job(name = "UpdateSmsStatus %0")
    public void run(EmailMessageStatusUpdateJobRequest emailMessageStatusUpdateJobRequest) throws Exception {
        emailService.updateStatus(emailMessageStatusUpdateJobRequest.getProviderMessageId(),
                emailMessageStatusUpdateJobRequest.getMessageStatus(), emailMessageStatusUpdateJobRequest.getRawStatus(),
                emailMessageStatusUpdateJobRequest.getProviderError());
    }
}
