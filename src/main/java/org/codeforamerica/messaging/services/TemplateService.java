package org.codeforamerica.messaging.services;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.exceptions.ElementNotFoundException;
import org.codeforamerica.messaging.exceptions.EmptyTemplateVariantsException;
import org.codeforamerica.messaging.exceptions.TemplateExistsException;
import org.codeforamerica.messaging.exceptions.TemplateInUseException;
import org.codeforamerica.messaging.models.Template;
import org.codeforamerica.messaging.models.TemplateVariant;
import org.codeforamerica.messaging.models.TemplateVariantRequest;
import org.codeforamerica.messaging.repositories.MessageRepository;
import org.codeforamerica.messaging.repositories.TemplateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public Template getTemplateByName(String name) {
        return templateRepository.findFirstByNameIgnoreCase(name.strip()).orElseThrow(() ->
                new ElementNotFoundException("Template not found: %s".formatted(name)));
    }

    public Template createTemplate(Template template) {
        Optional<Template> existingTemplate = templateRepository.findFirstByNameIgnoreCase(template.getName().strip());
        if (existingTemplate.isPresent()) {
            throw new TemplateExistsException("Template name is taken");
        }
        if (template.getTemplateVariants().isEmpty()) {
            throw new EmptyTemplateVariantsException("At least one template variant is required");
        }
        template.getTemplateVariants().forEach(templateVariant -> templateVariant.setTemplate(template));
        return templateRepository.save(template);
    }

    public void deleteTemplateAndVariants(String templateName) {
        Template template = getTemplateByName(templateName);
        if (template.getTemplateVariants().stream().anyMatch(this::isTemplateVariantInUse)) {
            throw new TemplateInUseException("At least one template variant is currently in use and cannot be deleted");
        }
        templateRepository.delete(template);
    }

    public Template modifyTemplateVariants(String templateName, Set<TemplateVariant> newTemplateVariants) {
        Template template = getTemplateByName(templateName);
        if (isAnyTemplateVariantInUse(template, newTemplateVariants)) {
            throw new TemplateInUseException("Cannot update a template variant that is already in use, list not updated");
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
            TemplateVariantRequest templateVariantRequest) {
        Template template = getTemplateByName(templateName);
        template.mergeTemplateVariant(TemplateVariant.builder()
                .subject(templateVariantRequest.getSubject())
                .emailBody(templateVariantRequest.getEmailBody())
                .smsBody(templateVariantRequest.getSmsBody())
                .language(language)
                .treatment(treatment)
                .build());
        return templateRepository.save(template);
    }

    public Template deleteTemplateVariant(String templateName, String language, String treatment) {
        Template template = getTemplateByName(templateName);
        TemplateVariant templateVariant = template.getTemplateVariant(language, treatment).orElseThrow(() ->
                new ElementNotFoundException("TemplateVariant not found: name=%s; language=%s; treatment=%s"
                        .formatted(templateName, language, treatment)));
        if (isTemplateVariantInUse(templateVariant)) {
            throw new TemplateInUseException("Template variant is currently in use and cannot be deleted");
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
