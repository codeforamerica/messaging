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
import java.util.Optional;
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
        return templateList.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(templateList);
    }

    @GetMapping("/{name}")
    @Operation(summary = "Get the all versions of a template and their variants")
    public ResponseEntity<Set<Template>> getTemplatesByName(@PathVariable String name) {
        Set<Template> templateSet = templateService.getAllTemplatesByName(name);
        return templateSet.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(templateSet);
    }

    @GetMapping("/{name}/{version}")
    @Operation(summary = "Get a template version and its variants")
    public ResponseEntity<Template> getTemplateByNameAndVersion(@PathVariable String name, @PathVariable int version) {
        Optional<Template> template = templateService.getTemplateByNameAndVersion(name, version);
        return template.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a template with at least one variant")
    public ResponseEntity<Template> createTemplate(@Valid @RequestBody Template template) throws Exception {
        Template createdTemplate = templateService.createTemplate(template);
        Template updatedTemplate = templateService.getTemplateById(createdTemplate.getId()).get();

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{name}/{version}")
                .buildAndExpand(updatedTemplate.getName(), updatedTemplate.getVersion()).toUri();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location);
        return new ResponseEntity<>(createdTemplate, responseHeaders, HttpStatus.CREATED);
    }

    @DeleteMapping("/{name}/{version}")
    @Operation(summary = "Delete a template version and its variants")
    public ResponseEntity<?> deleteTemplate(@PathVariable String name, @PathVariable int version) throws Exception {
        templateService.deleteTemplateAndVariants(name, version);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{name}/{version}")
    @Operation(summary = "Create or modify a list of template variants")
    public ResponseEntity<Template> modifyTemplateVariants(@PathVariable String name, @PathVariable int version,
            @RequestBody Set<@Valid TemplateVariant> templateVariants) throws Exception {
        Template modifiedTemplate = templateService.modifyTemplateVariants(name, version, templateVariants);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{name}/{version}")
                .buildAndExpand(modifiedTemplate.getName(), modifiedTemplate.getVersion()).toUri();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location);
        return new ResponseEntity<>(modifiedTemplate, responseHeaders, HttpStatus.CREATED);
    }

    @PostMapping("/{name}/{version}/draft_copy")
    @Operation(summary = "Start a draft of a template based on an existing version")
    public ResponseEntity<Template> startNewDraft(@PathVariable String name, @PathVariable int version) {
        Template newTemplate = templateService.createDraftCopy(name, version);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{name}/{version}")
                .buildAndExpand(newTemplate.getName(), newTemplate.getVersion()).toUri();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location);
        return new ResponseEntity<>(newTemplate, responseHeaders, HttpStatus.CREATED);
    }

    @PutMapping("/{name}/{version}/archive")
    @Operation(summary = "Archive a template version")
    public ResponseEntity<Template> archiveTemplateVersion(@PathVariable String name, @PathVariable int version)
            throws Exception {
        Template archivedTemplate = templateService.archiveTemplateVersion(name, version);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{name}/{version}")
                .buildAndExpand(archivedTemplate.getName(), archivedTemplate.getVersion()).toUri();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location);
        return new ResponseEntity<>(archivedTemplate, responseHeaders, HttpStatus.CREATED);
    }

    @PutMapping("/{name}/{version}/activate")
    @Operation(summary = "Activate a draft template version and archive the existing active version")
    public ResponseEntity<Template> activateTemplateVersion(@PathVariable String name, @PathVariable int version)
            throws Exception {
        Template activeTemplate = templateService.activateTemplateVersion(name, version);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{name}/{version}")
                .buildAndExpand(activeTemplate.getName(), activeTemplate.getVersion()).toUri();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location);
        return new ResponseEntity<>(activeTemplate, responseHeaders, HttpStatus.CREATED);
    }

    @DeleteMapping("/{name}/{version}/{language}/{treatment}")
    @Operation(summary = "Delete a single template variant")
    public ResponseEntity<Template> deleteTemplateVariant(
            @PathVariable String name,
            @PathVariable int version,
            @PathVariable String language,
            @PathVariable String treatment) throws Exception {
        Template modifiedTemplate = templateService.deleteTemplateVariant(name, version, language, treatment);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{name}/{version}")
                .buildAndExpand(modifiedTemplate.getName(), modifiedTemplate.getVersion()).toUri();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location);
        return new ResponseEntity<>(modifiedTemplate, responseHeaders, HttpStatus.OK);
    }

    @PutMapping("/{name}/{version}/{language}/{treatment}")
    @Operation(summary = "Create or modify a single template variant")
    public ResponseEntity<Template> createOrUpdateTemplateVariant(
            @PathVariable String name,
            @PathVariable int version,
            @PathVariable String language,
            @PathVariable String treatment,
            @Valid @RequestBody TemplateVariantRequest templateVariantRequest) throws Exception {
        Template modifiedTemplate = templateService.mergeTemplateVariant(name, version, language, treatment, templateVariantRequest);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{name}/{version}")
                .buildAndExpand(modifiedTemplate.getName(), modifiedTemplate.getVersion()).toUri();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setLocation(location);
        return new ResponseEntity<>(modifiedTemplate, responseHeaders, HttpStatus.CREATED);
    }
}
