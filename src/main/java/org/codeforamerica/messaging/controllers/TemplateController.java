package org.codeforamerica.messaging.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.codeforamerica.messaging.models.Template;
import org.codeforamerica.messaging.models.TemplateVariant;
import org.codeforamerica.messaging.models.TemplateVariantRequest;
import org.codeforamerica.messaging.services.TemplateService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(("/api/v1/templates"))
public class TemplateController {
    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping()
    @Operation(summary = "Get the list of all templates and their variants")
    public ResponseEntity<List<Template>> getTemplateList() {
        List<Template> templateList = templateService.getTemplateList();
        return templateList.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(templateService.getTemplateList());
    }

    @GetMapping("/{name}")
    @Operation(summary = "Get a template and its variants")
    public ResponseEntity<Template> getTemplateByName(@PathVariable String name) {
        Template template = templateService.getTemplateByName(name);
        return new ResponseEntity<>(template, HttpStatus.OK);
    }

    @PostMapping
    @Operation(summary = "Create a template with at least one variant")
    public ResponseEntity<Template> createTemplate(@Valid @RequestBody Template template) {
        Template createdTemplate = templateService.createTemplate(template);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{name}")
                .buildAndExpand(createdTemplate.getName()).toUri();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location);
        return new ResponseEntity<>(createdTemplate, responseHeaders, HttpStatus.CREATED);
    }

    @DeleteMapping("/{name}")
    @Operation(summary = "Delete a template and its variants")
    public ResponseEntity<?> deleteTemplate(@PathVariable String name) {
        templateService.deleteTemplateAndVariants(name);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{name}")
    @Operation(summary = "Create or modify a list of template variants")
    public ResponseEntity<Template> modifyTemplateVariants(@PathVariable String name, @RequestBody Set<@Valid TemplateVariant> templateVariants) {
        Template modifiedTemplate = templateService.modifyTemplateVariants(name, templateVariants);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{name}")
                .buildAndExpand(modifiedTemplate.getName()).toUri();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location);
        return new ResponseEntity<>(modifiedTemplate, responseHeaders, HttpStatus.CREATED);
    }

    @DeleteMapping("/{name}/{language}/{treatment}")
    @Operation(summary = "Delete a single template variant")
    public ResponseEntity<Template> deleteTemplateVariant(
            @PathVariable String name,
            @PathVariable String language,
            @PathVariable String treatment) {
        Template modifiedTemplate = templateService.deleteTemplateVariant(name, language, treatment);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{name}")
                .buildAndExpand(modifiedTemplate.getName()).toUri();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location);
        return new ResponseEntity<>(modifiedTemplate, responseHeaders, HttpStatus.OK);
    }

    @PutMapping("/{name}/{language}/{treatment}")
    @Operation(summary = "Create or modify a single template variant")
    public ResponseEntity<Template> createOrUpdateTemplateVariant(
            @PathVariable String name,
            @PathVariable String language,
            @PathVariable String treatment,
            @Valid @RequestBody TemplateVariantRequest templateVariantRequest) {
        Template modifiedTemplate = templateService.mergeTemplateVariant(name, language, treatment, templateVariantRequest);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{name}")
                .buildAndExpand(modifiedTemplate.getName()).toUri();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location);
        return new ResponseEntity<>(modifiedTemplate, responseHeaders, HttpStatus.CREATED);
    }
}
