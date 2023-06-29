package org.codeforamerica.messaging.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
    Set<TemplateVariant> templateVariants = new HashSet<>();
    @CreationTimestamp
    @JsonIgnore
    private OffsetDateTime creationTimestamp;
    @UpdateTimestamp
    @JsonIgnore
    private OffsetDateTime updateTimestamp;

    public void addTemplateVariant(TemplateVariant templateVariant) throws Exception {
        if (this.templateVariants.contains(templateVariant)) {
            throw new Exception("Template variant already exists");
        } else {
            this.templateVariants.add(templateVariant);
            templateVariant.setTemplate(this);
        }
    }

    public void updateTemplateVariant(TemplateVariant templateVariant, String body, String subject) {
        templateVariant.setBody(body);
        templateVariant.setSubject(subject);
    }

    public void mergeTemplateVariant(TemplateVariant templateVariant) throws Exception {
        Optional<TemplateVariant> existingTemplateVariant =
                this.getTemplateVariant(templateVariant.getLanguage(), templateVariant.getTreatment());
        if (existingTemplateVariant.isPresent()) {
            this.updateTemplateVariant(
                    existingTemplateVariant.get(),
                    templateVariant.getBody(),
                    templateVariant.getSubject()
            );
        } else {
            this.addTemplateVariant(templateVariant);
        }
    }

    public void removeTemplateVariant(TemplateVariant templateVariant) throws Exception {
        if (this.getTemplateVariants().size() == 1) {
            throw new Exception("Cannot delete last variant on template - delete parent template instead");
        }
        templateVariant.setTemplate(null);
        this.getTemplateVariants().remove(templateVariant);
    }

    public Optional<TemplateVariant> getTemplateVariant(String language, String treatment) {
        return this.getTemplateVariants().stream()
                .filter(templateVariant -> templateVariant.getLanguage().equals(language))
                .filter(templateVariant -> templateVariant.getTreatment().equals(treatment))
                .findAny();
    }
}
