package org.codeforamerica.messaging.services;

import org.codeforamerica.messaging.TestData;
import org.codeforamerica.messaging.exceptions.DuplicateTemplateException;
import org.codeforamerica.messaging.exceptions.EmptyTemplateVariantsException;
import org.codeforamerica.messaging.exceptions.TemplateInUseException;
import org.codeforamerica.messaging.models.Message;
import org.codeforamerica.messaging.models.Template;
import org.codeforamerica.messaging.models.TemplateVariant;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.repositories.TemplateRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TemplateServiceTest {
    @Autowired
    TemplateService templateService;
    @Autowired
    TemplateRepository templateRepository;
    @Autowired
    MessageRepository messageRepository;

    @BeforeEach
    void setup() {
    }

    @AfterEach
    void tearDown() {
        messageRepository.deleteAll();
        templateRepository.deleteAll();
    }

    @Test
    void whenCreatingAValidTemplate_thenSaveTemplate() {
        Template template = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template);

        template = templateService.createTemplate(template);
        assertNotNull(templateRepository.findFirstByNameIgnoreCase(template.getName()));
    }

    @Test
    void whenCreatingATemplateWithMissingTemplateVariants_thenDoNotSaveTemplate() {
        Template template = TestData.aTemplate().build();
        assertThrows(EmptyTemplateVariantsException.class, () -> templateService.createTemplate(template));
    }

    @Test
    void whenCreatingATemplateWithADuplicateName_thenDoNotSaveTemplate() {
        templateRepository.save(TestData.aTemplate().build());
        Template template2 = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template2);
        assertThrows(DuplicateTemplateException.class, () -> templateService.createTemplate(template2));
    }

    @Test
    void whenDeletingAnUnusedTemplate_thenDoNotThrowAnException() {
        Template template = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template);
        templateRepository.save(template);

        templateService.deleteTemplateAndVariants(TestData.TEMPLATE_NAME);
        assertTrue(templateRepository.findFirstByNameIgnoreCase(TestData.TEMPLATE_NAME).isEmpty());
    }

    @Test
    void whenDeletingATemplateWithOneUnusedTemplateVariantAndOneUsed_thenDoNotChangeAnything() {
        Template template = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template);
        templateRepository.save(template);
        TemplateVariant templateVariant = template.getTemplateVariant("en", "A").get();
        Message message = TestData.aMessage(templateVariant).build();
        messageRepository.save(message);

        assertThrows(TemplateInUseException.class, () -> templateService.deleteTemplateAndVariants(TestData.TEMPLATE_NAME));
        assertEquals(Optional.of(template), templateRepository.findFirstByNameIgnoreCase(template.getName()));
    }

    @Test
    void whenAddingNonDuplicateTemplateVariants_thenAddAllTemplateVariants() {
        Template template = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template);
        templateRepository.save(template);

        Set<TemplateVariant> newTemplateVariants = Set.of(
                TestData.aTemplateVariant().language("es").build(),
                TestData.aTemplateVariant().treatment("B").build()
        );
        template = templateService.modifyTemplateVariants(TestData.TEMPLATE_NAME, newTemplateVariants);
        assertEquals(4, template.getTemplateVariants().size());
    }

    @Test
    void whenAddingDuplicateOfUnusedTemplateVariant_thenUpdateDuplicateTemplateVariant() {
        Template template = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template);
        templateRepository.save(template);

        Set<TemplateVariant> newTemplateVariants = Set.of(TestData.aTemplateVariant()
                .smsBody("new body")
                .emailBody(null)
                .subject(null)
                .build());
        template = templateService.modifyTemplateVariants(TestData.TEMPLATE_NAME, newTemplateVariants);
        assertEquals(2, template.getTemplateVariants().size());
        assertEquals("new body", template.getTemplateVariant("en", "A").get().getSmsBody());
        assertNull(template.getTemplateVariant("en", "A").get().getEmailBody());
        assertNull(template.getTemplateVariant("en", "A").get().getSubject());
    }

    @Test
    void whenAddingDuplicateOfUsedTemplateVariant_thenRejectTheDuplicate() {
        Template template = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template);
        template = templateRepository.save(template);
        TemplateVariant templateVariant = template.getTemplateVariant("en", "A").get();
        Message message = TestData.aMessage(templateVariant).build();
        messageRepository.save(message);

        Set<TemplateVariant> newTemplateVariants = Set.of(TestData.aTemplateVariant().smsBody("new body").subject(null).build());
        assertThrows(TemplateInUseException.class, () -> templateService.modifyTemplateVariants(TestData.TEMPLATE_NAME, newTemplateVariants));
        assertEquals(Optional.of(template), templateRepository.findFirstByNameIgnoreCase(template.getName()));
    }

    @Test
    @Transactional
    void whenDeletingTheLastTemplateVariant_thenDoNotDelete() {
        Template template = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template);
        templateRepository.save(template);

        template = templateService.deleteTemplateVariant(TestData.TEMPLATE_NAME, "es", "B");
        assertTrue(template.getTemplateVariant("es", "B").isEmpty());
        assertThrows(EmptyTemplateVariantsException.class, () ->
                templateService.deleteTemplateVariant(TestData.TEMPLATE_NAME, "en", "A"));
        assertFalse(template.getTemplateVariants().isEmpty());
    }

    @Test
    void whenDeletingAUsedTemplateVariant_thenDoNotDelete() {
        Template template = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template);
        templateRepository.save(template);
        TemplateVariant templateVariant = template.getTemplateVariant("es", "B").get();
        Message message = TestData.aMessage(templateVariant).build();
        messageRepository.save(message);

        assertThrows(TemplateInUseException.class, () ->
                templateService.deleteTemplateVariant(TestData.TEMPLATE_NAME, "es", "B"));
        assertTrue(template.getTemplateVariant("es", "B").isPresent());
    }

}
