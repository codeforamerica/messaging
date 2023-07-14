package org.codeforamerica.messaging.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.codeforamerica.messaging.validators.ValidMessageable;
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
@Builder(toBuilder = true)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(of = {"name", "version"})
public class Template {
    public enum Status {
        DRAFT, ACTIVE, ARCHIVED
    }

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;
    @NotBlank
    @Column(unique=true)
    @ToString.Include
    String name;
    @NotNull
    @Builder.Default
    private int version = 1;
    @NotBlank
    @Builder.Default
    private String status = Status.DRAFT.name();
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

    public void addTemplateVariant(TemplateVariant templateVariant) throws Exception {
        if (this.templateVariants.contains(templateVariant)) {
            throw new Exception("Template variant already exists");
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

    public void mergeTemplateVariant(TemplateVariant templateVariant) throws Exception {
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

    public void removeTemplateVariant(TemplateVariant templateVariant) throws Exception {
        if (this.getTemplateVariants().size() == 1) {
            throw new Exception("Cannot delete last variant on template - delete parent template instead");
        }
        this.getTemplateVariants().removeIf(tv -> tv.equals(templateVariant));
    }

    public Optional<TemplateVariant> getTemplateVariant(String language, String treatment) {
        return this.getTemplateVariants().stream()
                .filter(templateVariant -> templateVariant.getLanguage().equals(language))
                .filter(templateVariant -> templateVariant.getTreatment().equals(treatment))
                .findAny();
    }
}
