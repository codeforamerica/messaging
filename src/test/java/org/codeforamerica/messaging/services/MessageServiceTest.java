package org.codeforamerica.messaging.services;

import org.assertj.core.api.Assertions;
import org.codeforamerica.messaging.TestData;
import org.codeforamerica.messaging.jobs.SendMessageBatchJobRequest;
import org.codeforamerica.messaging.jobs.SendMessageJobRequest;
import org.codeforamerica.messaging.models.*;
import org.codeforamerica.messaging.repositories.*;
import org.jobrunr.jobs.lambdas.JobRequest;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;

@SpringBootTest
class MessageServiceTest {

    @Autowired
    MessageService messageService;
    @MockBean
    SmsService smsService;
    @MockBean
    EmailService emailService;
    @MockBean
    JobRequestScheduler jobRequestScheduler;
    @Autowired
    MessageRepository messageRepository;
    @Autowired
    SmsMessageRepository smsMessageRepository;
    @Autowired
    EmailMessageRepository emailMessageRepository;
    @Autowired
    TemplateRepository templateRepository;
    @Autowired
    MessageBatchRepository messageBatchRepository;

    Template template;

    @BeforeEach
    void setup() throws Exception {
        template = TestData.aTemplate().build();
        template = templateRepository.save(template);
        TestData.addVariantsToTemplate(template);
        template = templateRepository.save(template);
    }

    @AfterEach
    void tearDown() {
        messageRepository.deleteAll();
        messageBatchRepository.deleteAll();
        templateRepository.deleteAll();
    }

    @Test
    void whenOnlyPhone_thenOnlySmsServiceCalled() {
        Message message = messageService.saveMessage(TestData.aMessageRequest().toPhone(TestData.TO_PHONE).build(), null);

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService).sendSmsMessage(TestData.TO_PHONE, TestData.TEMPLATE_BODY_DEFAULT);
        Mockito.verify(emailService, Mockito.never()).sendEmailMessage(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void whenOnlyEmail_thenOnlyEmailServiceCalled() {
        Message message = messageService.saveMessage(TestData.aMessageRequest().toEmail(TestData.TO_EMAIL).build(), null);

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService, Mockito.never()).sendSmsMessage(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(emailService).sendEmailMessage(TestData.TO_EMAIL, TestData.TEMPLATE_BODY_DEFAULT, TestData.TEMPLATE_SUBJECT_DEFAULT);
    }

    @Test
    void whenBothPhoneAndEmail_thenBothServicesCalled() {
        MessageRequest messageRequest = TestData.aMessageRequest()
                .toPhone(TestData.TO_PHONE)
                .toEmail(TestData.TO_EMAIL)
                .build();
        Message message = messageService.saveMessage(messageRequest, null);

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService).sendSmsMessage(TestData.TO_PHONE, TestData.TEMPLATE_BODY_DEFAULT);
        Mockito.verify(emailService).sendEmailMessage(TestData.TO_EMAIL, TestData.TEMPLATE_BODY_DEFAULT, TestData.TEMPLATE_SUBJECT_DEFAULT);
    }

    @Test
    void whenMessageRequestHasLanguageAndTreatment_thenValuesAreUsedToSelectTemplateVariant() {
        MessageRequest messageRequest = TestData.aMessageRequest()
                .toPhone(TestData.TO_PHONE)
                .toEmail(TestData.TO_EMAIL)
                .templateParams(Map.of(
                        "language", "es",
                        "treatment", "B",
                        "placeholder", "{{placeholder}}"))
                .build();
        Message message = messageService.saveMessage(messageRequest, null);

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService).sendSmsMessage(messageRequest.getToPhone(), TestData.TEMPLATE_BODY_ES_B);
        Mockito.verify(emailService).sendEmailMessage(message.getToEmail(), TestData.TEMPLATE_BODY_ES_B, TestData.TEMPLATE_SUBJECT_ES_B);
    }

    @Test
    void whenEnqueueingMessageBatch_thenSendMessageBatchJobRequestIsEnqueued() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );

        MessageBatchRequest messageBatchRequest = MessageBatchRequest.builder()
                .templateName(TestData.TEMPLATE_NAME)
                .recipients(file)
                .build();
        MessageBatch messageBatch = messageService.enqueueMessageBatch(messageBatchRequest);
        Mockito.verify(jobRequestScheduler).enqueue((JobRequest) argThat( x -> ((SendMessageBatchJobRequest) x).getMessageBatchId().equals(messageBatch.getId())));
    }

    @Test
    void whenSchedulingMessageBatch_thenThatManySendMessageJobRequestsAreEnqueued() throws IOException {
        String recipients = """
                phone, email
                1234567890,bar@example.org
                8885551212,foo@example.com
                """;

        MessageBatch messageBatch = MessageBatch.builder()
                .template(template)
                .recipients(recipients.getBytes())
                .build();
        messageBatchRepository.save(messageBatch);

        messageService.scheduleMessageBatch(messageBatch.getId());
        Mockito.verify(jobRequestScheduler, times(2)).schedule(
                (OffsetDateTime) any(),
                isA(SendMessageJobRequest.class)
        );
        Assertions.assertThat(messageRepository.findMessagesByMessageBatchId(messageBatch.getId()).stream().map(Message::getToPhone))
                .containsExactlyInAnyOrderElementsOf(List.of("8885551212", "1234567890"));
        Assertions.assertThat(messageRepository.findMessagesByMessageBatchId(messageBatch.getId()).stream().map(Message::getToEmail))
                .containsExactlyInAnyOrderElementsOf(List.of("bar@example.org", "foo@example.com"));
     }


    @Test
    @Transactional
    public void whenMessagesInBatchHaveDifferentStatuses_ThenGetReturnsCorrectStatusCounts() throws Exception {
        MessageBatch originalMessageBatch = TestData.aMessageBatch().template(template).build();
        messageBatchRepository.save(originalMessageBatch);
        addMessage(originalMessageBatch, "delivered", "rejected");
        addMessage(originalMessageBatch, "delivered", "undelivered");
        addMessage(originalMessageBatch, null, "accepted");
        addMessage(originalMessageBatch, "accepted", null);

        MessageBatch messageBatch = messageService.getMessageBatch(originalMessageBatch.getId()).get();
        int[] metricsArray = new int[] {
                messageBatch.getMetrics().getAcceptedEmailCount(),
                messageBatch.getMetrics().getRejectedEmailCount(),
                messageBatch.getMetrics().getDeliveredEmailCount(),
                messageBatch.getMetrics().getUndeliveredEmailCount(),
                messageBatch.getMetrics().getAcceptedSmsCount(),
                messageBatch.getMetrics().getRejectedSmsCount(),
                messageBatch.getMetrics().getDeliveredSmsCount(),
                messageBatch.getMetrics().getUndeliveredSmsCount(),
        };
        Assertions.assertThat(metricsArray).isEqualTo(new int[] {1, 0, 2, 0, 1, 1, 0, 1});
    }

    private void addMessage(MessageBatch originalMessageBatch, String emailStatus, String smsStatus) {
        Message message = TestData.aMessage(originalMessageBatch.getTemplate().getTemplateVariants().stream().findFirst().get())
                .messageBatch(originalMessageBatch)
                .build();
        if (emailStatus != null) {
            EmailMessage emailMessage = TestData.anEmailMessage().message(message).status(emailStatus).build();
            emailMessage = emailMessageRepository.save(emailMessage);
            message.setEmailMessage(emailMessage);
        }
        if (smsStatus != null) {
            SmsMessage smsMessage = TestData.anSmsMessage().message(message).status(smsStatus).build();
            smsMessage = smsMessageRepository.save(smsMessage);
            message.setSmsMessage(smsMessage);
        }
        messageRepository.save(message);
    }

}
