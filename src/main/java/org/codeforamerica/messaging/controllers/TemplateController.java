package org.codeforamerica.messaging.controllers;

import org.codeforamerica.messaging.models.Template;
import org.codeforamerica.messaging.services.TemplateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(("/api/v1/templates"))
public class TemplateController {
    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping()
    public ResponseEntity<List<Template>> getTemplateList() {
        List<Template> templateList = templateService.getTemplateList();
        return templateList.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(templateService.getTemplateList());
    }

    @GetMapping("/{name}")
    public ResponseEntity<Template> getTemplateByName(@PathVariable String name) {
        Optional<Template> template = templateService.getTemplateByName(name);
        return template.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
