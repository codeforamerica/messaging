package org.codeforamerica.messaging.models;

import com.github.jknack.handlebars.Handlebars;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class Template {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    String name;
    String subject;
    @NotBlank
    String body;
    @CreationTimestamp
    private OffsetDateTime creationTimestamp;
    @UpdateTimestamp
    private OffsetDateTime updateTimestamp;


    public String buildBodyFromTemplate(Map<String, Object> templateParams) {
        return buildContentFromTemplate(templateParams, Template::getBody);
    }

    public String buildContentFromTemplate(
            Map<String, Object> templateParams,
            Function<Template, String> templateFieldGetter) {
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
