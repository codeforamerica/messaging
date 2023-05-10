package org.codeforamerica.messaging.models;

import com.github.jknack.handlebars.Handlebars;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "language", "variant"})})
public class Template {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    String name;
    String subject;
    @NotBlank
    String body;
    @NotBlank
    @Builder.Default
    String language = "en";
    @NotBlank
    @Builder.Default
    String variant = "A";
    @CreationTimestamp
    private OffsetDateTime creationTimestamp;
    @UpdateTimestamp
    private OffsetDateTime updateTimestamp;


    public String build(Function<Template, String> templateFieldGetter, Map<String, Object> templateParams) {
        Handlebars handlebars = new Handlebars();
        com.github.jknack.handlebars.Template handlebarsTemplate;
        try {
            handlebarsTemplate = handlebars.compileInline(templateFieldGetter.apply(this));
        } catch (Exception e) {
            throw new RuntimeException("Invalid template syntax", e);
        }

        try {
            return handlebarsTemplate.apply(templateParams);
        } catch (IOException e) {
            throw new RuntimeException("Invalid template parameters", e);
        }
    }
}
