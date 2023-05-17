package org.codeforamerica.messaging.models;

import com.github.jknack.handlebars.Handlebars;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
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
public class TemplateVariant {
    public static final String DEFAULT_LANGUAGE = "en";
    public static final String DEFAULT_TREATMENT = "A";

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    String subject;
    @NotBlank
    String body;
    @NotBlank
    @Builder.Default
    String language = DEFAULT_LANGUAGE;
    @NotBlank
    @Builder.Default
    String treatment = DEFAULT_TREATMENT;
    @ManyToOne
    Template template;
    @CreationTimestamp
    @ToString.Exclude
    private OffsetDateTime creationTimestamp;
    @UpdateTimestamp
    @ToString.Exclude
    private OffsetDateTime updateTimestamp;

    public String build(Function<TemplateVariant, String> templateFieldGetter, Map<String, Object> templateParams) throws IOException {
        Handlebars handlebars = new Handlebars();
        String templateField = templateFieldGetter.apply(this);
        if (templateField != null) {
            return handlebars.compileInline(templateField).apply(templateParams);
        }
        return null;
    }
}
