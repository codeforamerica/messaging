package org.codeforamerica.messaging.controllers;

import org.apache.commons.lang3.StringUtils;
import org.codeforamerica.messaging.models.Template;
import org.codeforamerica.messaging.services.TemplateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<Template>> getTemplateList(@RequestParam(required = false) String name) {
        if (StringUtils.isNotEmpty(name)) {
            Optional<Template> template = templateService.getTemplateByName(name);
            return template.map(value -> ResponseEntity.ok(List.of(value))).orElseGet(() -> ResponseEntity.notFound().build());
        }
        List<Template> templateList = templateService.getTemplateList();
        if (templateList.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(templateList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Template> getTemplateById(@PathVariable Long id) {
        Optional<Template> template = templateService.getTemplateById(id);
        return template.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
