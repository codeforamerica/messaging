package org.codeforamerica.messaging.services;

import org.codeforamerica.messaging.TestData;
import org.codeforamerica.messaging.models.*;
import org.codeforamerica.messaging.providers.mailgun.MailgunGateway;
import org.codeforamerica.messaging.repositories.EmailMessageRepository;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.repositories.TemplateRepository;
import org.jobrunr.JobRunrException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class EmailServiceStatusUpdateTest {
    @Autowired
    TemplateRepository templateRepository;
    @MockBean
    MailgunGateway mailgunGateway;
    @Autowired
    EmailService emailService;

    @Autowired
    EmailMessageRepository emailMessageRepository;
    @Autowired
    MessageRepository messageRepository;


    Template template;
    TemplateVariant templateVariant;

    @BeforeEach
    void setup() {
        template = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template);
        template = templateRepository.save(template);
        templateVariant = template.getTemplateVariants().stream().findAny().get();
    }

    @AfterEach
    void tearDown() {
        messageRepository.deleteAll();
        emailMessageRepository.deleteAll();
        templateRepository.deleteAll();
    }


    @Test
    public void whenNewStatusBeforeOld_ThenDoNothing() {
        Message message = TestData.aMessage(templateVariant).emailStatus(MessageStatus.sent).build();
        message = messageRepository.save(message);
        EmailMessage emailMessage = TestData.anEmailMessage().message(message).providerMessageId(TestData.PROVIDER_MESSAGE_ID).build();
        emailMessageRepository.save(emailMessage);
        message.setEmailMessage(emailMessage);
        message = messageRepository.save(message);

        emailService.updateStatus(emailMessage.getProviderMessageId(), MessageStatus.queued, "queued", null);
        Message updatedMessage = messageRepository.findById(message.getId()).get();
        assertEquals(MessageStatus.sent, updatedMessage.getEmailStatus());
    }

    @Test
    public void whenNewStatusAfterOld_ThenUpdate() {
        Message message = TestData.aMessage(templateVariant).smsStatus(MessageStatus.sent).build();
        message = messageRepository.save(message);
        EmailMessage emailMessage = TestData.anEmailMessage().message(message).providerMessageId(TestData.PROVIDER_MESSAGE_ID).build();
        emailMessageRepository.save(emailMessage);
        message.setEmailMessage(emailMessage);
        message = messageRepository.save(message);

        emailService.updateStatus(emailMessage.getProviderMessageId(), MessageStatus.delivered, "delivered", null);
        Message updatedMessage = messageRepository.findById(message.getId()).get();
        assertEquals(MessageStatus.delivered, updatedMessage.getEmailStatus());
    }

    @Test
    public void whenNoOldStatus_ThenUpdateToNew() {
        Message message = TestData.aMessage(templateVariant).emailStatus(null).build();
        message = messageRepository.save(message);
        EmailMessage emailMessage = TestData.anEmailMessage().message(message).providerMessageId(TestData.PROVIDER_MESSAGE_ID).build();
        emailMessageRepository.save(emailMessage);
        message.setEmailMessage(emailMessage);
        message = messageRepository.save(message);

        emailService.updateStatus(emailMessage.getProviderMessageId(), MessageStatus.delivered, "delivered", null);
        Message updatedMessage = messageRepository.findById(message.getId()).get();
        assertEquals(MessageStatus.delivered, updatedMessage.getEmailStatus());
    }

    @Test
    public void whenMessageNotFound_ThenThrowException() {
        assertThrows(JobRunrException.class, () -> emailService.updateStatus("invalid_provider_message_id", MessageStatus.queued, "queued", null));
    }

}
