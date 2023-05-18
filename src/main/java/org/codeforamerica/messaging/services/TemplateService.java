package org.codeforamerica.messaging.services;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.models.Template;
import org.codeforamerica.messaging.repositories.TemplateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public Optional<Template> getTemplateById(Long id) {
        return templateRepository.findById(id);
    }

    public Optional<Template> getTemplateByName(String name) {
        return templateRepository.findFirstByNameIgnoreCase(name.strip());
    }
}
