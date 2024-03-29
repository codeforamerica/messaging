package org.codeforamerica.messaging.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.codeforamerica.messaging.exceptions.DuplicateTemplateVariantException;
import org.codeforamerica.messaging.exceptions.EmptyTemplateVariantsException;
import org.codeforamerica.messaging.validators.ValidMessageable;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(of = {"name"})
public class Template {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;
    @NotBlank
    @Column(unique=true)
    @ToString.Include
    String name;
    @ToString.Include
    @Builder.Default
    @OneToMany(mappedBy = "template", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    Set<@ValidMessageable TemplateVariant> templateVariants = new HashSet<>();
    @CreationTimestamp
    @JsonIgnore
    private OffsetDateTime creationTimestamp;
    @UpdateTimestamp
    @JsonIgnore
    private OffsetDateTime updateTimestamp;

    public void addTemplateVariant(TemplateVariant templateVariant) {
        if (this.templateVariants.contains(templateVariant)) {
            throw new DuplicateTemplateVariantException("Template variant already exists");
        } else {
            this.templateVariants.add(templateVariant);
            templateVariant.setTemplate(this);
        }
    }

    public void updateTemplateVariant(TemplateVariant templateVariant, String smsBody, String emailBody, String subject) {
        templateVariant.setSmsBody(smsBody);
        templateVariant.setEmailBody(emailBody);
        templateVariant.setSubject(subject);
    }

    public void mergeTemplateVariant(TemplateVariant templateVariant) {
        Optional<TemplateVariant> existingTemplateVariant =
                this.getTemplateVariant(templateVariant.getLanguage(), templateVariant.getTreatment());
        if (existingTemplateVariant.isPresent()) {
            this.updateTemplateVariant(
                    existingTemplateVariant.get(),
                    templateVariant.getSmsBody(),
                    templateVariant.getEmailBody(),
                    templateVariant.getSubject()
            );
        } else {
            this.addTemplateVariant(templateVariant);
        }
    }

    public void removeTemplateVariant(TemplateVariant templateVariant) {
        if (this.getTemplateVariants().size() == 1) {
            throw new EmptyTemplateVariantsException(
                    "Cannot delete last variant on template - delete parent template instead");
        }
        this.getTemplateVariants().removeIf(tv -> tv.equals(templateVariant));
    }

    public Optional<TemplateVariant> getTemplateVariant(String language, String treatment) {
        return this.getTemplateVariants().stream()
                .filter(templateVariant -> templateVariant.getLanguage().equals(language))
                .filter(templateVariant -> templateVariant.getTreatment().equals(treatment))
                .findAny();
    }

    @JsonIgnore
    public Set<String> getAllPlaceholders() {
        return this.templateVariants.stream()
                .map(TemplateVariant::getAllPlaceholders)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
}
