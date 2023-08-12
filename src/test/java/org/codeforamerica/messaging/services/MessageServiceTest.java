package org.codeforamerica.messaging.services;

import org.codeforamerica.messaging.TestData;
import org.codeforamerica.messaging.exceptions.MessageSendException;
import org.codeforamerica.messaging.exceptions.MissingCSVHeadersException;
import org.codeforamerica.messaging.jobs.SendMessageBatchJobRequest;
import org.codeforamerica.messaging.jobs.SendMessageJobRequest;
import org.codeforamerica.messaging.models.*;
import org.codeforamerica.messaging.repositories.*;
import org.jobrunr.jobs.lambdas.JobRequest;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.messaging.utils.CSVReader.*;
import static org.junit.jupiter.api.Assertions.*;
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
    void setup() {
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
    void whenOnlyPhone_thenOnlySmsServiceCalled() throws MessageSendException {
        Message message = messageService.saveMessage(TestData.aMessageRequest().toPhone(TestData.TO_PHONE).build(), null);

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService).sendSmsMessage(TestData.TO_PHONE, TestData.TEMPLATE_BODY_DEFAULT);
        Mockito.verify(emailService, Mockito.never()).sendEmailMessage(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void whenOnlyEmail_thenOnlyEmailServiceCalled() throws MessageSendException {
        Message message = messageService.saveMessage(TestData.aMessageRequest().toEmail(TestData.TO_EMAIL).build(), null);

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService, Mockito.never()).sendSmsMessage(Mockito.anyString(), Mockito.anyString());
        ArgumentCaptor<String> emailBodyCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(emailService).sendEmailMessage(eq(TestData.TO_EMAIL), emailBodyCaptor.capture(), eq(TestData.TEMPLATE_SUBJECT_DEFAULT));
        assertTrue(emailBodyCaptor.getValue().contains(TestData.TEMPLATE_BODY_DEFAULT));
        assertTrue(emailBodyCaptor.getValue().contains("To unsubscribe click: %unsubscribe_url%"));
    }

    @Test
    void whenBothPhoneAndEmail_thenBothServicesCalled() throws MessageSendException {
        MessageRequest messageRequest = TestData.aMessageRequest()
                .toPhone(TestData.TO_PHONE)
                .toEmail(TestData.TO_EMAIL)
                .build();
        Message message = messageService.saveMessage(messageRequest, null);

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService).sendSmsMessage(TestData.TO_PHONE, TestData.TEMPLATE_BODY_DEFAULT);
        ArgumentCaptor<String> emailBodyCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(emailService).sendEmailMessage(eq(TestData.TO_EMAIL), emailBodyCaptor.capture(), eq(TestData.TEMPLATE_SUBJECT_DEFAULT));
        assertTrue(emailBodyCaptor.getValue().contains(TestData.TEMPLATE_BODY_DEFAULT));
        assertTrue(emailBodyCaptor.getValue().contains("To unsubscribe click: %unsubscribe_url%"));
    }

    @Test
    void whenMessageRequestHasLanguageAndTreatment_thenValuesAreUsedToSelectTemplateVariant() throws MessageSendException {
        MessageRequest messageRequest = TestData.aMessageRequest()
                .toPhone(TestData.TO_PHONE)
                .toEmail(TestData.TO_EMAIL)
                .templateParams(Map.of(
                        LANGUAGE_HEADER, "es",
                        TREATMENT_HEADER, "B",
                        "placeholder", "{{{placeholder}}}"))
                .build();
        Message message = messageService.saveMessage(messageRequest, null);

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService).sendSmsMessage(messageRequest.getToPhone(), TestData.TEMPLATE_BODY_ES_B);
        ArgumentCaptor<String> emailBodyCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(emailService).sendEmailMessage(eq(message.getToEmail()), emailBodyCaptor.capture(), eq(TestData.TEMPLATE_SUBJECT_ES_B));
        assertTrue(emailBodyCaptor.getValue().contains(TestData.TEMPLATE_BODY_ES_B));
        assertTrue(emailBodyCaptor.getValue().contains("Para darse de baja haga clic: %unsubscribe_url%"));
    }

    @Test
    void whenEnqueueingMessageBatch_thenSendMessageBatchJobRequestIsEnqueued() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "phone, email, placeholder".getBytes()
        );

        MessageBatchRequest messageBatchRequest = MessageBatchRequest.builder()
                .templateName(TestData.TEMPLATE_NAME)
                .recipients(file)
                .build();
        MessageBatch messageBatch = messageService.enqueueMessageBatch(messageBatchRequest);
        Mockito.verify(jobRequestScheduler).enqueue((JobRequest) argThat( x -> ((SendMessageBatchJobRequest) x).getMessageBatchId().equals(messageBatch.getId())));
    }

    @Test
    void whenSchedulingMessageBatch_thenThatManySendMessageJobRequestsAreEnqueued() {
        String recipients = """
                phone, email, placeholder
                1234567890,bar@example.org, placeholder
                8885551212,foo@example.com, placeholder
                """;

        MessageBatch messageBatch = MessageBatch.builder()
                .template(template)
                .recipients(recipients.getBytes())
                .build();
        messageBatchRepository.save(messageBatch);

        messageService.scheduleMessagesInBatch(messageBatch.getId());
        Mockito.verify(jobRequestScheduler, times(2)).schedule(
                (OffsetDateTime) any(),
                isA(SendMessageJobRequest.class)
        );
        assertThat(messageRepository.findMessagesByMessageBatchId(messageBatch.getId()).stream().map(Message::getToPhone))
                .containsExactlyInAnyOrderElementsOf(List.of("8885551212", "1234567890"));
        assertThat(messageRepository.findMessagesByMessageBatchId(messageBatch.getId()).stream().map(Message::getToEmail))
                .containsExactlyInAnyOrderElementsOf(List.of("bar@example.org", "foo@example.com"));
     }


    @Test
    @Transactional
    public void whenMessagesInBatchHaveDifferentStatuses_ThenGetReturnsCorrectStatusCounts() {
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
        assertThat(metricsArray).isEqualTo(new int[] {1, 0, 2, 0, 1, 1, 0, 1});
    }

    @Test
    public void whenRecipientsFileIsMissingHeader_ThenThrowException() {
        String recipients = """
                phone, email
                1234567890, bar@example.org, placeholder
                8885551212, foo@example.com, placeholder
                """;

        MessageBatchRequest messageBatchRequest = MessageBatchRequest.builder()
                .templateName(template.getName())
                .recipients(new MockMultipartFile("testfile", recipients.getBytes()))
                .build();

        assertThrows(MissingCSVHeadersException.class, () -> messageService.enqueueMessageBatch(messageBatchRequest));
    }

    @Test
    public void whenRecipientInBatchHaveMissingData_ThenReportInvalidRow() {
        String recipients = """
                phone, email, placeholder
                1234567890, bar@example.org
                8885551212, foo@example.com, placeholder
                """;

        MessageBatch messageBatch = MessageBatch.builder()
                .template(template)
                .recipients(recipients.getBytes())
                .build();
        messageBatchRepository.save(messageBatch);

        messageService.scheduleMessagesInBatch(messageBatch.getId());
        Mockito.verify(jobRequestScheduler, times(1)).schedule(
                (OffsetDateTime) any(),
                isA(SendMessageJobRequest.class)
        );
        assertThat(messageRepository.findMessagesByMessageBatchId(messageBatch.getId()).stream().map(Message::getToPhone))
                .containsExactly("8885551212");
        assertEquals(1, messageBatchRepository.findById(messageBatch.getId()).get().getRecipientErrorRows().size());
        assertEquals("Missing template parameters: [placeholder]",
                messageBatchRepository.findById(messageBatch.getId()).get().getRecipientErrorRows().stream()
                        .map(row -> row.get(ERROR_HEADER)).findFirst().get());
    }


    private void addMessage(MessageBatch originalMessageBatch, String emailStatus, String smsStatus) {
        Message message = TestData.aMessage(originalMessageBatch.getTemplate().getTemplateVariants().stream().findFirst().get())
                .messageBatch(originalMessageBatch)
                .smsStatus(smsStatus)
                .emailStatus(emailStatus)
                .build();
        messageRepository.save(message);
    }

}
