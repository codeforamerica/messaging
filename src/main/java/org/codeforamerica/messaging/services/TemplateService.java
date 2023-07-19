package org.codeforamerica.messaging.services;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.models.Template;
import org.codeforamerica.messaging.models.TemplateVariant;
import org.codeforamerica.messaging.models.TemplateVariantRequest;
import org.codeforamerica.messaging.repositories.MessageBatchRepository;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.repositories.TemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final MessageRepository messageRepository;
    private final MessageBatchRepository messageBatchRepository;

    public TemplateService(TemplateRepository templateRepository,
            MessageRepository messageRepository,
            MessageBatchRepository messageBatchRepository) {
        this.templateRepository = templateRepository;
        this.messageRepository = messageRepository;
        this.messageBatchRepository = messageBatchRepository;
    }

    public List<Template> getTemplateList() {
        return (List<Template>) templateRepository.findAll();
    }

    public Optional<Template> getTemplateById(Long id) {
        return templateRepository.findById(id);
    }

    public Optional<Template> getActiveTemplateByName(String name) {
        return templateRepository.findFirstActiveByNameIgnoreCase(name.strip());
    }

    public Set<Template> getAllTemplatesByName(String name) {
        return templateRepository.findAllByNameIgnoreCase(name.strip());
    }

    public Optional<Template> getTemplateByNameAndVersion(String name, int version) {
        return templateRepository.findFirstByNameIgnoreCaseAndVersion(name, version);
    }

    @Transactional
    public Template createTemplate(Template template) throws Exception {
        Optional<Template> existingTemplate = getActiveTemplateByName(template.getName());
        if (existingTemplate.isPresent()) {
            throw new Exception("Template name is taken");
        }
        if (template.getTemplateVariants().isEmpty()) {
            throw new Exception("At least one template variant is required");
        }
        template.getTemplateVariants().forEach(templateVariant -> templateVariant.setTemplate(template));
        return templateRepository.save(template);
    }

    public Template createDraftCopy(String templateName, int version) {
        Template existingActiveTemplate = templateRepository.findFirstByNameIgnoreCaseAndVersion(templateName, version)
                .orElseThrow(NoSuchElementException::new);
        Template newTemplate = existingActiveTemplate.toBuilder()
                .id(null)
                .status(Template.Status.DRAFT.name())
                .templateVariants(new HashSet<>())
                .build();
        newTemplate.setTemplateVariants(existingActiveTemplate.getTemplateVariants().stream()
                        .map(TemplateVariant::toBuilder)
                        .map(newVariantBuilder -> newVariantBuilder.id(null))
                        .map(newVariantBuilder -> newVariantBuilder.template(newTemplate))
                        .map(TemplateVariant.TemplateVariantBuilder::build)
                        .collect(Collectors.toSet()));
        return templateRepository.save(newTemplate);
    }

    public Template archiveTemplateVersion(String templateName, int version) throws Exception {
        Optional<Template> existingTemplate = templateRepository.findFirstByNameIgnoreCaseAndVersion(templateName, version);
        if (existingTemplate.isEmpty() || existingTemplate.get().getStatus().equals(Template.Status.ARCHIVED.name())) {
            return null;
        }
        Template activeTemplate = existingTemplate.get();
        if (isBatchWithTemplatePending(activeTemplate)) {
            throw new Exception("Cannot archive template until all pending batches are sent. TemplateName=%s, version=%s"
                    .formatted(templateName, activeTemplate.getVersion()));
        }
        log.info("Archiving template. TemplateName=%s, version=%s".formatted(templateName, activeTemplate.getVersion()));
        activeTemplate.setStatus(Template.Status.ARCHIVED.name());
        return templateRepository.save(activeTemplate);
    }

    public Template activateTemplateVersion(String templateName, int version) throws Exception {
        Optional<Template> existingTemplate = templateRepository.findFirstByNameIgnoreCaseAndVersion(templateName, version);
        if (existingTemplate.isEmpty() || !existingTemplate.get().getStatus().equals(Template.Status.DRAFT.name())) {
            return null;
        }
        Optional<Template> optionalActiveTemplate = templateRepository.findFirstActiveByNameIgnoreCase(templateName);
        if (optionalActiveTemplate.isPresent()) {
            Template activeTemplate = optionalActiveTemplate.get();
            try {
                archiveTemplateVersion(templateName, activeTemplate.getVersion());
            } catch (Exception e) {
                throw new Exception("Cannot activate template v%s until active template v%s can be archived. TemplateName=%s"
                        .formatted(existingTemplate.get().getVersion(), activeTemplate.getVersion(), templateName));
            }
        }
        existingTemplate.get().setStatus(Template.Status.ACTIVE.name());
        return templateRepository.save(existingTemplate.get());
    }

    public void deleteTemplateAndVariants(String templateName, int version) throws Exception {
        Template template = templateRepository.findFirstByNameIgnoreCaseAndVersion(templateName, version)
                .orElseThrow(NoSuchElementException::new);
        if (template.getTemplateVariants().stream().anyMatch(this::isTemplateVariantInUse)) {
            throw new Exception("At least one template variant is currently in use and cannot be deleted");
        }
        templateRepository.delete(template);
    }

    public Template modifyTemplateVariants(String templateName, int version, Set<TemplateVariant> newTemplateVariants) throws Exception {
        Template template = templateRepository.findFirstByNameIgnoreCaseAndVersion(templateName, version)
                .orElseThrow(NoSuchElementException::new);
        if (isAnyTemplateVariantInUse(template, newTemplateVariants) && template.getStatus().equals(Template.Status.DRAFT.name())) {
            throw new Exception("Cannot update a template variant that is active or already in use, list not updated");
        }
        newTemplateVariants.forEach(templateVariant -> {
            try {
                template.mergeTemplateVariant(templateVariant);
            } catch (Exception ignored) {}
        });
        return templateRepository.save(template);
    }

    public Template mergeTemplateVariant(
            String templateName,
            int version,
            String language,
            String treatment,
            TemplateVariantRequest templateVariantRequest) throws Exception {
        Template template = templateRepository.findFirstByNameIgnoreCaseAndVersion(templateName, version)
                .orElseThrow(NoSuchElementException::new);
        template.mergeTemplateVariant(TemplateVariant.builder()
                .subject(templateVariantRequest.getSubject())
                .emailBody(templateVariantRequest.getEmailBody())
                .smsBody(templateVariantRequest.getSmsBody())
                .language(language)
                .treatment(treatment)
                .build());
        return templateRepository.save(template);
    }

    public Template deleteTemplateVariant(String templateName, int version, String language, String treatment) throws Exception {
        Template template = templateRepository.findFirstByNameIgnoreCaseAndVersion(templateName, version)
                .orElseThrow(NoSuchElementException::new);
        TemplateVariant templateVariant = template.getTemplateVariant(language, treatment).orElseThrow(NoSuchElementException::new);
        if (isTemplateVariantInUse(templateVariant) || !template.getStatus().equals(Template.Status.DRAFT.name())) {
            throw new Exception("Only draft template variants that are not in use may be deleted.");
        }
        template.removeTemplateVariant(templateVariant);
        return templateRepository.save(template);
    }

    public boolean isAnyTemplateVariantInUse(Template template, Set<TemplateVariant> templateVariants) {
        return templateVariants.stream()
                .map(tv -> template.getTemplateVariant(tv.getLanguage(), tv.getTreatment()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .anyMatch(this::isTemplateVariantInUse);
    }

    public boolean isTemplateVariantInUse(TemplateVariant templateVariant) {
        return templateVariant.getId() != null && messageRepository.countByTemplateVariantId(templateVariant.getId()) > 0;
    }

    public boolean isBatchWithTemplatePending(Template template) {
        return messageBatchRepository.countByTemplateIdAndSendAtIsAfter(template.getId(), OffsetDateTime.now()) > 0;
    }
}
