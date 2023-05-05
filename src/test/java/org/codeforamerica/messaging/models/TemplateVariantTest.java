package org.codeforamerica.messaging.models;

import org.codeforamerica.messaging.repositories.TemplateSetRepository;
import org.codeforamerica.messaging.repositories.TemplateVariantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class TemplateVariantTest {
    @Autowired
    private TemplateSetRepository templateSetRepository;
    @Autowired
    private TemplateVariantRepository templateVariantRepository;


    private final String SUBJECT = "Hi {{recipient_name}}! First SMS ever!";
    private final String BODY = "Merry Christmas {{recipient_name}}! From {{sender_name}}";
    private final TemplateSet TEMPLATE_SET = TemplateSet.builder()
            .name("test")
            .build();
    private final TemplateVariant TEMPLATE_VARIANT = TemplateVariant.builder()
            .body(BODY)
            .subject(SUBJECT)
            .templateSet(TEMPLATE_SET)
            .build();

    @BeforeEach
    void setup() {
        templateSetRepository.save(TEMPLATE_SET);
        templateVariantRepository.save(TEMPLATE_VARIANT);
    }

    @AfterEach
    void tearDown() {
        templateVariantRepository.deleteAll();
        templateSetRepository.deleteAll();
    }

    @Test
    void whenInputHasTemplateParams_thenTemplateUsesParams() throws IOException {
        Map<String, Object> templateParams = Map.of("recipient_name", "Jarvis", "sender_name", "Papworth");

        Assertions.assertEquals("Hi Jarvis! First SMS ever!",
                TEMPLATE_VARIANT.build(TemplateVariant::getSubject, templateParams));
        Assertions.assertEquals("Merry Christmas Jarvis! From Papworth",
                TEMPLATE_VARIANT.build(TemplateVariant::getBody, templateParams));
    }

    @Test
    public void whenSavingDuplicateTemplateWithMatchingLanguageAndTreatment_thenAnExceptionIsThrown() {
        TemplateVariant templateVariantDuplicate = TemplateVariant.builder()
                .body(BODY)
                .templateSet(TEMPLATE_SET)
                .build();
        assertThrows(DataIntegrityViolationException.class, () ->
                templateVariantRepository.save(templateVariantDuplicate)
        );
    }

    @Test
    public void whenSavingDuplicateTemplateWithDifferentLanguage_thenTheTemplateIsSaved() {
        TemplateVariant newLanguageVariant = TemplateVariant.builder()
                .templateSet(TEMPLATE_SET)
                .body(BODY)
                .language("es_ES")
                .build();
        templateVariantRepository.save(newLanguageVariant);
    }

    @Test
    public void whenSavingDuplicateTemplateWithDifferentTreatement_thenTheTemplateIsSaved() {
        TemplateVariant newTreatmentVariant = TemplateVariant.builder()
                .templateSet(TEMPLATE_SET)
                .body(BODY)
                .treatment("B")
                .build();
        templateVariantRepository.save(newTreatmentVariant);
    }
}
