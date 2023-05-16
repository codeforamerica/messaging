package org.codeforamerica.messaging.services;

import org.codeforamerica.messaging.models.*;
import org.codeforamerica.messaging.repositories.EmailMessageRepository;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.repositories.SmsMessageRepository;
import org.codeforamerica.messaging.repositories.TemplateRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

@SpringBootTest
class MessageServiceTest {

    @Autowired
    MessageService messageService;
    @MockBean
    SmsService smsService;
    @MockBean
    EmailService emailService;
    @Autowired
    MessageRepository messageRepository;
    @Autowired
    SmsMessageRepository smsMessageRepository;
    @Autowired
    EmailMessageRepository emailMessageRepository;
    @Autowired
    TemplateRepository templateRepository;

    public static final String TEMPLATE_NAME = "test";
    private final String SUBJECT = "Any subject";
    private final String BODY = "Any body";
    private final String SPANISH_SUBJECT = "Spanish subject";
    private final String SPANISH_BODY = "Spanish body";
    private final Template TEMPLATE = Template.builder()
            .name(TEMPLATE_NAME)
            .build();
    private final TemplateVariant TEMPLATE_VARIANT = TemplateVariant.builder()
            .body(BODY)
            .subject(SUBJECT)
            .template(TEMPLATE)
            .build();
    private final TemplateVariant TEMPLATE_VARIANT_ES_B = TemplateVariant.builder()
            .body(SPANISH_BODY)
            .subject(SPANISH_SUBJECT)
            .template(TEMPLATE)
            .language("es")
            .treatment("B")
            .build();

    @BeforeEach
    void setup() {
        TEMPLATE.setTemplateVariants((List.of(TEMPLATE_VARIANT, TEMPLATE_VARIANT_ES_B)));
        templateRepository.save(TEMPLATE);
    }

    @AfterEach
    void tearDown() {
        messageRepository.deleteAll();
        templateRepository.deleteAll();
    }

    @Test
    void whenOnlyPhone_thenOnlySmsServiceCalled() {
        MessageRequest messageRequest = MessageRequest.builder()
                .toPhone("8005551212")
                .templateName(TEMPLATE_NAME)
                .build();
        Message message = messageService.saveMessage(messageRequest);

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService).sendSmsMessage(messageRequest.getToPhone(), BODY);
        Mockito.verify(emailService, Mockito.never()).sendEmailMessage(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void whenOnlyEmail_thenOnlyEmailServiceCalled() {
        MessageRequest messageRequest = MessageRequest.builder()
                .toEmail("foo@example.com")
                .templateName(TEMPLATE_NAME)
                .build();
        Message message = messageService.saveMessage(messageRequest);

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService, Mockito.never()).sendSmsMessage(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(emailService).sendEmailMessage(message.getToEmail(), BODY, SUBJECT);
    }

    @Test
    void whenBothPhoneAndEmail_thenBothServicesCalled() {
        MessageRequest messageRequest = MessageRequest.builder()
                .toPhone("8005551212")
                .toEmail("foo@example.com")
                .templateName(TEMPLATE_NAME)
                .build();
        Message message = messageService.saveMessage(messageRequest);

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService).sendSmsMessage(messageRequest.getToPhone(), BODY);
        Mockito.verify(emailService).sendEmailMessage(message.getToEmail(), BODY, SUBJECT);
    }

    @Test
    void whenScheduledWithBothPhoneAndEmail_thenBothServicesCalledAfterScheduleDelay() {
        String subject = "Any subject";
        String body = "Any body";
        MessageRequest messageRequest = MessageRequest.builder()
                .toPhone("8005551212")
                .toEmail("foo@example.com")
                .templateName(TEMPLATE_NAME)
                .sendAt(OffsetDateTime.now().plusSeconds(20))
                .build();
        SmsMessage smsMessage = SmsMessage.builder()
                .body(body)
                .toPhone(messageRequest.getToPhone())
                .fromPhone("1234567890")
                .status("accepted")
                .providerMessageId("PROVIDER_MESSAGE_ID")
                .build();
        smsMessageRepository.save(smsMessage);

        Mockito.when(smsService.sendSmsMessage(Mockito.any(), Mockito.any())).thenReturn(smsMessage);

        EmailMessage emailMessage = EmailMessage.builder()
                .body(body)
                .subject(subject)
                .toEmail(messageRequest.getToEmail())
                .fromEmail("messaging@example.com")
                .status("accepted")
                .providerMessageId("PROVIDER_MESSAGE_ID")
                .build();
        Mockito.when(emailService.sendEmailMessage(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(emailMessage);
        emailMessageRepository.save(emailMessage);

        Message message = messageService.scheduleMessage(messageRequest);
        await().atMost(60, SECONDS).until(() -> {
            var m = messageRepository.findById(message.getId()).get();
            return m.getSmsMessage() != null && m.getEmailMessage() != null;
        });
    }

    @Test
    void whenMessageRequestHasLanguageAndTreatment_thenValuesAreUsedToSelectTemplateVariant() {
        MessageRequest messageRequest = MessageRequest.builder()
                .toPhone("8005551212")
                .toEmail("foo@example.com")
                .templateName(TEMPLATE_NAME)
                .templateParams(Map.of("language", "es", "treatment", "B"))
                .build();
        Message message = messageService.saveMessage(messageRequest);

        messageService.sendMessage(message.getId());
        Mockito.verify(smsService).sendSmsMessage(messageRequest.getToPhone(), SPANISH_BODY);
        Mockito.verify(emailService).sendEmailMessage(message.getToEmail(), SPANISH_BODY, SPANISH_SUBJECT);
    }

}
