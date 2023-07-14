package org.codeforamerica.messaging.services;

import org.codeforamerica.messaging.TestData;
import org.codeforamerica.messaging.models.Message;
import org.codeforamerica.messaging.models.MessageBatch;
import org.codeforamerica.messaging.models.Template;
import org.codeforamerica.messaging.models.TemplateVariant;
import org.codeforamerica.messaging.repositories.MessageBatchRepository;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.repositories.TemplateRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
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
    @Autowired
    MessageBatchRepository messageBatchRepository;

    @BeforeEach
    void setup() {
    }

    @AfterEach
    void tearDown() {
        messageBatchRepository.deleteAll();
        messageRepository.deleteAll();
        templateRepository.deleteAll();
    }

    @Test
    void whenCreatingAValidTemplate_thenSaveTemplate() throws Exception {
        Template template = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template);

        template = templateService.createTemplate(template);
        assertNotNull(templateRepository.findFirstActiveByNameIgnoreCase(template.getName()));
    }

    @Test
    void whenCreatingATemplateWithMissingTemplateVariants_thenDoNotSaveTemplate() {
        Template template = TestData.aTemplate().build();
        assertThrows(Exception.class, () -> templateService.createTemplate(template));
    }

    @Test
    void whenCreatingATemplateWithADuplicateName_thenDoNotSaveTemplate() throws Exception {
        templateRepository.save(TestData.aTemplate().build());
        Template template2 = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template2);
        assertThrows(Exception.class, () -> templateService.createTemplate(template2));
    }

    @Test
    void whenCreatingADraftCopyTemplate_thenNewDraftTemplateIsCreatedWithNewTemplateVariants() throws Exception {
        Template template = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template);
        template = templateRepository.save(template);

        Template templateDraft = templateService.createDraftCopy(template.getName(), template.getVersion());
        templateRepository.save(templateDraft);
        assertEquals(Template.Status.ACTIVE.name(), template.getStatus());
        assertEquals(Template.Status.DRAFT.name(), templateDraft.getStatus());
        assertEquals(2, templateDraft.getVersion());
        assertEquals(template.getTemplateVariants().size(), templateDraft.getTemplateVariants().size());
        assertNotEquals(template.getId(), templateDraft.getId());
        assertFalse(template
                .getTemplateVariants().stream()
                .map(TemplateVariant::getId)
                .anyMatch(id -> templateDraft.getTemplateVariants().stream().map(TemplateVariant::getId).anyMatch(id::equals)));
    }

    @Test
    void whenArchivingAnActiveTemplateVersionWithABatchInThePast_thenTemplateVersionIsArchived() throws Exception {
        Template template = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template);
        template = templateRepository.save(template);
        MessageBatch messageBatch = TestData.aMessageBatch().template(template).sendAt(OffsetDateTime.now().plusSeconds(1L)).build();
        messageBatchRepository.save(messageBatch);
        Thread.sleep(1000);

        template = templateService.archiveTemplateVersion(template.getName(), template.getVersion());
        templateRepository.save(template);
        assertEquals(Template.Status.ARCHIVED.name(), template.getStatus());
        assertTrue(template
                .getTemplateVariants().stream()
                .map(TemplateVariant::getTemplateStatus)
                .allMatch(status -> status.equals(Template.Status.ARCHIVED.name())));
    }

    @Test
    void whenArchivingATemplateWithAPendingMessageBatch_thenTemplateIsNotArchived() throws Exception {
        Template template = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template);
        template = templateRepository.save(template);
        MessageBatch messageBatch = TestData.aMessageBatch().template(template).sendAt(OffsetDateTime.now().plusDays(1)).build();
        messageBatchRepository.save(messageBatch);

        Template finalTemplate = template;
        assertThrows(Exception.class, () -> templateService.archiveTemplateVersion(finalTemplate.getName(), finalTemplate.getVersion()));
        assertEquals(Template.Status.ACTIVE.name(), template.getStatus());
        assertTrue(template
                .getTemplateVariants().stream()
                .map(TemplateVariant::getTemplateStatus)
                .allMatch(status -> status.equals(Template.Status.ACTIVE.name())));
    }

    @Test
    void whenActivatingADraftTemplateWithAnExistingActiveTemplate_thenDraftIsActiveAndActiveIsArchived() throws Exception {
        Template activeTemplate = TestData.aTemplate().status(Template.Status.ACTIVE.name()).build();
        TestData.addVariantsToTemplate(activeTemplate);
        activeTemplate = templateRepository.save(activeTemplate);
        Template draftTemplate = templateService.createDraftCopy(activeTemplate.getName(), activeTemplate.getVersion());
        draftTemplate = templateRepository.save(draftTemplate);

        Template activatedDraftTemplate = templateService.activateTemplateVersion(draftTemplate.getName(), draftTemplate.getVersion());
        assertEquals(Template.Status.ACTIVE.name(), activatedDraftTemplate.getStatus());
        assertTrue(activatedDraftTemplate
                .getTemplateVariants().stream()
                .map(TemplateVariant::getTemplateStatus)
                .allMatch(status -> status.equals(Template.Status.ACTIVE.name())));

        Template archivedTemplate = templateRepository.findFirstByNameIgnoreCaseAndVersion(
                activeTemplate.getName(),
                activeTemplate.getVersion()).orElse(null);
        assertNotNull(archivedTemplate);
        assertEquals(Template.Status.ARCHIVED.name(), archivedTemplate.getStatus());
        assertTrue(archivedTemplate
                .getTemplateVariants().stream()
                .map(TemplateVariant::getTemplateStatus)
                .allMatch(status -> status.equals(Template.Status.ARCHIVED.name())));
    }

    @Test
    void whenDeletingAnUnusedTemplate_thenDoNotThrowAnException() throws Exception {
        Template template = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template);
        templateRepository.save(template);

        templateService.deleteTemplateAndVariants(TestData.TEMPLATE_NAME);
        assertTrue(templateRepository.findFirstActiveByNameIgnoreCase(TestData.TEMPLATE_NAME).isEmpty());
    }

    @Test
    void whenDeletingATemplateWithOneUnusedTemplateVariantAndOneUsed_thenDoNotChangeAnything() throws Exception {
        Template template = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template);
        templateRepository.save(template);
        TemplateVariant templateVariant = template.getTemplateVariant("en", "A").get();
        Message message = TestData.aMessage(templateVariant).build();
        messageRepository.save(message);

        assertThrows(Exception.class, () -> templateService.deleteTemplateAndVariants(TestData.TEMPLATE_NAME));
        assertEquals(Optional.of(template), templateRepository.findFirstActiveByNameIgnoreCase(template.getName()));
    }

    @Test
    void whenAddingNonDuplicateTemplateVariants_thenAddAllTemplateVariants() throws Exception {
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
    void whenAddingDuplicateOfUnusedTemplateVariant_thenUpdateDuplicateTemplateVariant() throws Exception {
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
    void whenAddingDuplicateOfUsedTemplateVariant_thenRejectTheDuplicate() throws Exception {
        Template template = TestData.aTemplate().build();
        TestData.addVariantsToTemplate(template);
        template = templateRepository.save(template);
        TemplateVariant templateVariant = template.getTemplateVariant("en", "A").get();
        Message message = TestData.aMessage(templateVariant).build();
        messageRepository.save(message);

        Set<TemplateVariant> newTemplateVariants = Set.of(TestData.aTemplateVariant().smsBody("new body").subject(null).build());
        assertThrows(Exception.class, () -> templateService.modifyTemplateVariants(TestData.TEMPLATE_NAME, newTemplateVariants));
        assertEquals(Optional.of(template), templateRepository.findFirstActiveByNameIgnoreCase(template.getName()));
    }

    @Test
    @Transactional
    void whenDeletingTheLastTemplateVariant_thenDoNotDelete() throws Exception {
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
    void whenDeletingAUsedTemplateVariant_thenDoNotDelete() throws Exception {
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
