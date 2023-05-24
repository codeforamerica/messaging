package org.codeforamerica.messaging.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class Template {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @Column(unique=true)
    @ToString.Include
    String name;
    @ToString.Include
    @Singular
    @OneToMany(mappedBy = "template", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    List<TemplateVariant> templateVariants = new LinkedList<>();
    @CreationTimestamp
    private OffsetDateTime creationTimestamp;
    @UpdateTimestamp
    private OffsetDateTime updateTimestamp;

    public TemplateVariant addTemplateVariant(TemplateVariant.TemplateVariantBuilder templateVariantBuilder) {
        if (templateVariants.isEmpty()) {
            this.setTemplateVariants(new LinkedList<>());
        }
        TemplateVariant templateVariant = templateVariantBuilder.template(this).build();
        templateVariants.add(templateVariant);
        return templateVariant;
    }
}
