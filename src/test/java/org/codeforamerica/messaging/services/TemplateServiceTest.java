package org.codeforamerica.messaging.services;

import lombok.SneakyThrows;
import org.codeforamerica.messaging.TestData;
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
    @SneakyThrows
    void whenCreatingAValidTemplate_thenSaveTemplate() {
        Template template = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template);

        template = templateService.createTemplate(template);
        assertNotNull(templateRepository.findFirstByNameIgnoreCase(template.getName()));
    }

    @Test
    void whenCreatingATemplateWithMissingTemplateVariants_thenDoNotSaveTemplate() {
        Template template = TestData.aTemplate().build();
        assertThrows(Exception.class, () -> templateService.createTemplate(template));
    }

    @Test
    void whenCreatingATemplateWithADuplicateName_thenDoNotSaveTemplate() {
        templateRepository.save(TestData.aTemplate().build());
        Template template2 = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template2);
        assertThrows(Exception.class, () -> templateService.createTemplate(template2));
    }

    @Test
    @SneakyThrows
    void whenDeletingAnUnusedTemplate_thenDoNotThrowAnException() {
        Template template = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template);
        templateRepository.save(template);

        templateService.deleteTemplateAndVariants(TestData.TEMPLATE_NAME);
        assertTrue(templateRepository.findFirstByNameIgnoreCase(TestData.TEMPLATE_NAME).isEmpty());
    }

    @Test
    @SneakyThrows
    void whenDeletingATemplateWithOneUnusedTemplateVariantAndOneUsed_thenDoNotChangeAnything() {
        Template template = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template);
        templateRepository.save(template);
        TemplateVariant templateVariant = template.getTemplateVariant("en", "A").get();
        Message message = TestData.aMessage(templateVariant).build();
        messageRepository.save(message);

        assertThrows(Exception.class, () -> templateService.deleteTemplateAndVariants(TestData.TEMPLATE_NAME));
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

        Set<TemplateVariant> newTemplateVariants = Set.of(
                TestData.aTemplateVariant().body("new body").subject(null).build()
        );
        template = templateService.modifyTemplateVariants(TestData.TEMPLATE_NAME, newTemplateVariants);
        assertEquals(2, template.getTemplateVariants().size());
        assertEquals("new body", template.getTemplateVariant("en", "A").get().getBody());
        assertNull(template.getTemplateVariant("en", "A").get().getSubject());
    }

    @Test
    void whenAddingDuplicateOfUsedTemplateVariant_thenRejectTheDuplicate() {
        Template template = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template);
        templateRepository.save(template);
        TemplateVariant templateVariant = template.getTemplateVariant("en", "A").get();
        Message message = TestData.aMessage(templateVariant).build();
        messageRepository.save(message);

        Set<TemplateVariant> newTemplateVariants = Set.of(
                TestData.aTemplateVariant().body("new body").subject(null).build()
        );
        assertThrows(Exception.class, () -> templateService.modifyTemplateVariants(TestData.TEMPLATE_NAME, newTemplateVariants));
        assertEquals(Optional.of(template), templateRepository.findFirstByNameIgnoreCase(template.getName()));
    }

    @Test
    @SneakyThrows
    void whenDeletingTheLastTemplateVariant_thenDoNotDelete() {
        Template template = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template);
        templateRepository.save(template);

        template = templateService.deleteTemplateVariant(TestData.TEMPLATE_NAME, "es", "B");
        assertTrue(template.getTemplateVariant("es", "B").isEmpty());
        assertThrows(Exception.class, () ->
                templateService.deleteTemplateVariant(TestData.TEMPLATE_NAME, "en", "A"));
        assertFalse(template.getTemplateVariants().isEmpty());
    }

    @Test
    @SneakyThrows
    void whenDeletingAUsedTemplateVariant_thenDoNotDelete() {
        Template template = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template);
        templateRepository.save(template);
        TemplateVariant templateVariant = template.getTemplateVariant("es", "B").get();
        Message message = TestData.aMessage(templateVariant).build();
        messageRepository.save(message);

        assertThrows(Exception.class, () ->
                templateService.deleteTemplateVariant(TestData.TEMPLATE_NAME, "es", "B"));
        assertTrue(template.getTemplateVariant("es", "B").isPresent());
    }

}
