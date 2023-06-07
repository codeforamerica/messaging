package org.codeforamerica.messaging.services;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.models.Template;
import org.codeforamerica.messaging.models.TemplateVariant;
import org.codeforamerica.messaging.models.TemplateVariantRequest;
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

    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
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
        if (template.getTemplateVariants().stream().anyMatch(templateVariant -> !templateVariant.getMessages().isEmpty())) {
            throw new Exception("At least one template variant is currently in use and cannot be deleted");
        }
        templateRepository.delete(template);
    }

    public Template modifyTemplateVariants(String templateName, Set<TemplateVariant> templateVariants) {
        Template template = getTemplateByName(templateName).orElseThrow(NoSuchElementException::new);
        templateVariants.forEach(templateVariant -> {
            try {
                template.mergeTemplateVariant(templateVariant);
            } catch (Exception e) {
                throw new RuntimeException("Could not complete update of template variant list", e);
            }
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
        template.removeTemplateVariant(language, treatment);
        return templateRepository.save(template);
    }
}
