package org.codeforamerica.messaging.jobs;

import org.codeforamerica.messaging.services.SmsService;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.lambdas.JobRequestHandler;
import org.springframework.stereotype.Component;

@Component
public class SmsMessageStatusUpdateJobRequestHandler implements JobRequestHandler<SmsMessageStatusUpdateJobRequest>  {

    private final SmsService smsService;

    public SmsMessageStatusUpdateJobRequestHandler(SmsService smsService) {
        this.smsService = smsService;
    }

    @Override
    @Job(name = "UpdateSmsStatus %0")
    public void run(SmsMessageStatusUpdateJobRequest smsMessageStatusUpdateJobRequest) throws Exception {
        smsService.updateStatus(smsMessageStatusUpdateJobRequest.getProviderMessageId(),
                smsMessageStatusUpdateJobRequest.getMessageStatus(), smsMessageStatusUpdateJobRequest.getRawStatus(),
                smsMessageStatusUpdateJobRequest.getFromPhone(), smsMessageStatusUpdateJobRequest.getProviderError());
    }
}
