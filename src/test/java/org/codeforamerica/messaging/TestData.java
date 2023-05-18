package org.codeforamerica.messaging;

import org.codeforamerica.messaging.models.*;
import org.codeforamerica.messaging.providers.twilio.TwilioGateway;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Map;

public class TestData {
    public static final String TO_PHONE = "1234567890";
    public static final String TO_EMAIL = "recipient@example.com";
    public static final String FROM_EMAIL = "sender@example.com";
    public static final String PROVIDER_MESSAGE_ID = "some-provider-message-id";
    public static final String STATUS = "accepted";
    public static final String TEMPLATE_NAME = "default template name";
    public static final String TEMPLATE_SUBJECT_DEFAULT = "English A Subject: {{placeholder}}";
    public static final String TEMPLATE_BODY_DEFAULT = "English A Body: {{placeholder}}";
    public static final String TEMPLATE_SUBJECT_EN_B = "English B Subject: {{placeholder}}";
    public static final String TEMPLATE_BODY_EN_B = "English B Body: {{placeholder}}";
    public static final String TEMPLATE_SUBJECT_ES_A = "Spanish A Subject: {{placeholder}}";
    public static final String TEMPLATE_BODY_ES_A = "Spanish A Body: {{placeholder}}";
    public static final String TEMPLATE_SUBJECT_ES_B = "Spanish B Subject: {{placeholder}}";
    public static final String TEMPLATE_BODY_ES_B = "Spanish B Body: {{placeholder}}";

    public static TemplateVariant.TemplateVariantBuilder aDefaultTemplateVariant() {
        return TemplateVariant.builder()
                .id(1L)
                .subject(TEMPLATE_SUBJECT_DEFAULT)
                .body(TEMPLATE_BODY_DEFAULT)
                .creationTimestamp(OffsetDateTime.now());
    }

    public static TemplateVariant.TemplateVariantBuilder anEnglishBTemplateVariant() {
        return TemplateVariant.builder()
                .id(2L)
                .subject(TEMPLATE_SUBJECT_EN_B)
                .body(TEMPLATE_BODY_EN_B)
                .treatment("B")
                .creationTimestamp(OffsetDateTime.now());
    }

    public static TemplateVariant.TemplateVariantBuilder aSpanishATemplateVariant() {
        return TemplateVariant.builder()
                .id(3L)
                .subject(TEMPLATE_SUBJECT_ES_A)
                .body(TEMPLATE_BODY_ES_A)
                .language("es")
                .creationTimestamp(OffsetDateTime.now());
    }

    public static TemplateVariant.TemplateVariantBuilder aSpanishBTemplateVariant() {
        return TemplateVariant.builder()
                .id(4L)
                .subject(TEMPLATE_SUBJECT_ES_B)
                .body(TEMPLATE_BODY_ES_B)
                .language("es")
                .treatment("B")
                .creationTimestamp(OffsetDateTime.now());
    }

    public static Template.TemplateBuilder aTemplate() {
        return Template.builder()
                .id(0L)
                .name(TEMPLATE_NAME)
                .templateVariants(new HashSet<>())
                .creationTimestamp(OffsetDateTime.now());
    }

    public static void addVariantsToTemplate(Template template) {
        template.addTemplateVariant(aDefaultTemplateVariant().build());
        template.addTemplateVariant(anEnglishBTemplateVariant().build());
        template.addTemplateVariant(aSpanishATemplateVariant().build());
        template.addTemplateVariant(aSpanishBTemplateVariant().build());
    }

    public static MessageRequest.MessageRequestBuilder aMessageRequest() {
        return MessageRequest.builder()
                .templateName(TestData.TEMPLATE_NAME)
                .templateParams(Map.of("placeholder", "{{placeholder}}"));
    }

    public static SmsMessage.SmsMessageBuilder anSmsMessage() {
        return SmsMessage.builder()
                .body(TestData.TEMPLATE_BODY_DEFAULT)
                .toPhone(TestData.TO_PHONE)
                .fromPhone(TwilioGateway.DEFAULT_FROM_PHONE)
                .status(TestData.STATUS)
                .providerMessageId(TestData.PROVIDER_MESSAGE_ID);
    }

    public static EmailMessage.EmailMessageBuilder anEmailMessage() {
        return EmailMessage.builder()
                .body(TestData.TEMPLATE_BODY_DEFAULT)
                .subject(TestData.TEMPLATE_SUBJECT_DEFAULT)
                .toEmail(TestData.TO_EMAIL)
                .fromEmail(TestData.FROM_EMAIL)
                .status(TestData.STATUS)
                .providerMessageId(TestData.PROVIDER_MESSAGE_ID);
    }

}
