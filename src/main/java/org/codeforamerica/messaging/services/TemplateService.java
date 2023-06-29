package org.codeforamerica.messaging.services;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.models.Template;
import org.codeforamerica.messaging.models.TemplateVariant;
import org.codeforamerica.messaging.models.TemplateVariantRequest;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.repositories.TemplateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final MessageRepository messageRepository;

    public TemplateService(TemplateRepository templateRepository, MessageRepository messageRepository) {
        this.templateRepository = templateRepository;
        this.messageRepository = messageRepository;
    }

    public List<Template> getTemplateList() {
        return (List<Template>) templateRepository.findAll();
    }

    public Optional<Template> getTemplateByName(String name) {
        return templateRepository.findFirstByNameIgnoreCase(name.strip());
    }

    public Template createTemplate(Template template) throws Exception {
        Optional<Template> existingTemplate = getTemplateByName(template.getName());
        if (existingTemplate.isPresent()) {
            throw new Exception("Template name is taken");
        }
        if (template.getTemplateVariants().isEmpty()) {
            throw new Exception("At least one template variant is required");
        }
        template.getTemplateVariants().forEach(templateVariant -> templateVariant.setTemplate(template));
        return templateRepository.save(template);
    }

    public void deleteTemplateAndVariants(String templateName) throws Exception {
        Template template = getTemplateByName(templateName).orElseThrow(NoSuchElementException::new);
        if (template.getTemplateVariants().stream().anyMatch(this::isTemplateVariantInUse)) {
            throw new Exception("At least one template variant is currently in use and cannot be deleted");
        }
        templateRepository.delete(template);
    }

    public Template modifyTemplateVariants(String templateName, Set<TemplateVariant> newTemplateVariants) throws Exception {
        Template template = getTemplateByName(templateName).orElseThrow(NoSuchElementException::new);
        if (isAnyTemplateVariantInUse(template, newTemplateVariants)) {
            throw new Exception("Cannot update a template variant that is already in use, list not updated");
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
            String language,
            String treatment,
            TemplateVariantRequest templateVariantRequest) throws Exception {
        Template template = getTemplateByName(templateName).orElseThrow(NoSuchElementException::new);
        template.mergeTemplateVariant(TemplateVariant.builder()
                .body(templateVariantRequest.getBody())
                .subject(templateVariantRequest.getSubject())
                .language(language)
                .treatment(treatment)
                .build());
        return templateRepository.save(template);
    }

    public Template deleteTemplateVariant(String templateName, String language, String treatment) throws Exception {
        Template template = getTemplateByName(templateName).orElseThrow(NoSuchElementException::new);
        TemplateVariant templateVariant = template.getTemplateVariant(language, treatment).orElseThrow(NoSuchElementException::new);
        if (isTemplateVariantInUse(templateVariant)) {
            throw new Exception("Template variant is currently in use and cannot be deleted");
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
}
