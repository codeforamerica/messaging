package org.codeforamerica.messaging.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.jknack.handlebars.Handlebars;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.validators.ValidMessageable;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.function.Function;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = {"template", "language", "treatment"})
@ValidMessageable
@Slf4j
public class TemplateVariant implements Messageable {
    public static final String DEFAULT_LANGUAGE = "en";
    public static final String DEFAULT_TREATMENT = "A";

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;
    @NotBlank
    @Builder.Default
    String language = DEFAULT_LANGUAGE;
    @NotBlank
    @Builder.Default
    String treatment = DEFAULT_TREATMENT;
    String subject;
    String emailBody;
    String smsBody;
    @ManyToOne
    @NotNull
    @JsonIgnore
    @ToString.Exclude
    Template template;
    @CreationTimestamp
    @JsonIgnore
    @ToString.Exclude
    private OffsetDateTime creationTimestamp;
    @UpdateTimestamp
    @JsonIgnore
    @ToString.Exclude
    private OffsetDateTime updateTimestamp;

    @JsonIgnore
    public String getTemplateName() {
        return template.getName();
    }

    public String build(Function<TemplateVariant, String> templateFieldGetter, Map<String, String> templateParams) {
        Handlebars handlebars = new Handlebars();
        String templateField = templateFieldGetter.apply(this);
        if (templateField != null) {
            try {
                return handlebars.compileInline(templateField).apply(templateParams);
            } catch (IOException e) {
                log.error("Error processing template field using getter " + templateFieldGetter + " in templateVariant #" + this.getId());
            }
        }
        return null;
    }
}
