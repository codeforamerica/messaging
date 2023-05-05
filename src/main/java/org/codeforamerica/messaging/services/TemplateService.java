package org.codeforamerica.messaging.services;

import com.github.jknack.handlebars.Handlebars;
import org.codeforamerica.messaging.models.Template;
import org.codeforamerica.messaging.repositories.TemplateRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

@Service
public class TemplateService {
    private final TemplateRepository templateRepository;

    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    public String buildBodyFromTemplate(String templateName, Map<String, Object> templateParams) {
        return buildContentFromTemplate(templateName, templateParams, Template::getBody);
    }

    public String buildContentFromTemplate(
            String templateName,
            Map<String, Object> templateParams,
            Function<Template, String> templateFieldGetter) {
        Handlebars handlebars = new Handlebars();
        Template template = templateRepository.findFirstByName(templateName);
        if (template == null) {
            throw new RuntimeException("Template not found with the name provided");
        }

        com.github.jknack.handlebars.Template handlebarsTemplate;
        try {
            handlebarsTemplate = handlebars.compileInline(templateFieldGetter.apply(template));
        } catch (Exception e) {
            throw new RuntimeException("Invalid template syntax", e);
        }

        try {
            return handlebarsTemplate.apply(templateParams);
        } catch (IOException e) {
            throw new RuntimeException("Invalid template parameters", e);
        }
    }
}
