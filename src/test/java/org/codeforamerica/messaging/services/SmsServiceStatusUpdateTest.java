package org.codeforamerica.messaging.services;

import org.codeforamerica.messaging.TestData;
import org.codeforamerica.messaging.models.*;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.repositories.SmsMessageRepository;
import org.codeforamerica.messaging.repositories.TemplateRepository;
import org.jobrunr.JobRunrException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class SmsServiceStatusUpdateTest {
    @Autowired
    SmsService smsService;
    @Autowired
    SmsMessageRepository smsMessageRepository;
    @Autowired
    MessageRepository messageRepository;
    @Autowired
    TemplateRepository templateRepository;

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
        smsMessageRepository.deleteAll();
        templateRepository.deleteAll();
    }

    @Test
    public void whenNewStatusBeforeOld_ThenDoNothing() {
        Message message = TestData.aMessage(templateVariant).smsStatus(MessageStatus.sent).build();
        message = messageRepository.save(message);
        SmsMessage smsMessage = TestData.anSmsMessage().message(message).providerMessageId(TestData.PROVIDER_MESSAGE_ID).build();
        smsMessageRepository.save(smsMessage);
        message.setSmsMessage(smsMessage);
        message = messageRepository.save(message);

        smsService.updateStatus(smsMessage.getProviderMessageId(), MessageStatus.queued, "queued", TestData.TO_PHONE, null);
        Message updatedMessage = messageRepository.findById(message.getId()).get();
        assertEquals(MessageStatus.sent, updatedMessage.getSmsStatus());
    }

    @Test
    public void whenNewStatusAfterOld_ThenUpdate() {
        Message message = TestData.aMessage(templateVariant).smsStatus(MessageStatus.sent).build();
        message = messageRepository.save(message);
        SmsMessage smsMessage = TestData.anSmsMessage().message(message).providerMessageId(TestData.PROVIDER_MESSAGE_ID).build();
        smsMessageRepository.save(smsMessage);
        message.setSmsMessage(smsMessage);
        message = messageRepository.save(message);

        smsService.updateStatus(smsMessage.getProviderMessageId(), MessageStatus.delivered, "delivered", TestData.TO_PHONE, null);
        Message updatedMessage = messageRepository.findById(message.getId()).get();
        assertEquals(MessageStatus.delivered, updatedMessage.getSmsStatus());
    }

    @Test
    public void whenNoOldStatus_ThenUpdateToNew() {
        Message message = TestData.aMessage(templateVariant).smsStatus(null).build();
        message = messageRepository.save(message);
        SmsMessage smsMessage = TestData.anSmsMessage().message(message).providerMessageId(TestData.PROVIDER_MESSAGE_ID).build();
        smsMessageRepository.save(smsMessage);
        message.setSmsMessage(smsMessage);
        message = messageRepository.save(message);

        smsService.updateStatus(smsMessage.getProviderMessageId(), MessageStatus.delivered, "delivered", TestData.TO_PHONE, null);
        Message updatedMessage = messageRepository.findById(message.getId()).get();
        assertEquals(MessageStatus.delivered, updatedMessage.getSmsStatus());

    }

    @Test
    public void whenMessageNotFound_ThenThrowException() {
        assertThrows(JobRunrException.class, () -> smsService.updateStatus("invalid_provider_message_id", MessageStatus.queued, "queued", TestData.TO_PHONE, null));
    }
}
