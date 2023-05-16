package org.codeforamerica.messaging.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
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
    @JsonIgnore
    @OneToMany(mappedBy = "template", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    List<TemplateVariant> templateVariants;
    @CreationTimestamp
    private OffsetDateTime creationTimestamp;
    @UpdateTimestamp
    private OffsetDateTime updateTimestamp;
}
