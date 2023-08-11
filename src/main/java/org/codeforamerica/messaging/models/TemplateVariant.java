package org.codeforamerica.messaging.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.TagType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.exceptions.InvalidParamsException;
import org.codeforamerica.messaging.exceptions.InvalidTemplateException;
import org.codeforamerica.messaging.exceptions.MissingParamsException;
import org.codeforamerica.messaging.validators.ValidMessageable;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

@Slf4j
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = {"template", "language", "treatment"})
@ValidMessageable
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
        com.github.jknack.handlebars.Template handlebarsTemplate = convertStringToHandlebarsTemplate(templateFieldGetter.apply(this));
        if (handlebarsTemplate == null) {
            return null;
        }
        Set<String> missingParams = getPlaceholdersInTemplateVariantField(handlebarsTemplate).stream()
                .filter(not(templateParams::containsKey))
                .collect(Collectors.toSet());
        if (!missingParams.isEmpty()) {
            throw new MissingParamsException(missingParams);
        }
        try {
            return handlebarsTemplate.apply(templateParams);
        } catch (IOException e) {
            throw new InvalidParamsException(templateParams.keySet(), e);
        }
    }

    com.github.jknack.handlebars.Template convertStringToHandlebarsTemplate(String templateString) {
        if (templateString == null) {
            return null;
        }
        Handlebars handlebars = new Handlebars();
        try {
            return handlebars.compileInline(templateString);
        } catch (Exception e) {
            throw new InvalidTemplateException("Not a valid handlebars template: %s".formatted(templateString), e);
        }
    }

    Set<String> getPlaceholdersInTemplateVariantField(com.github.jknack.handlebars.Template handlebarsTemplate) {
        Set<String> vars = new HashSet<>(handlebarsTemplate.collect(TagType.VAR));
        vars.addAll(handlebarsTemplate.collect(TagType.TRIPLE_VAR));
        return vars;
    }

    Set<String> getPlaceholdersInTemplateVariantField(Function<TemplateVariant, String> templateFieldGetter) {
        com.github.jknack.handlebars.Template handlebarsTemplate = convertStringToHandlebarsTemplate(templateFieldGetter.apply(this));
        return handlebarsTemplate == null ? Set.of() : getPlaceholdersInTemplateVariantField(handlebarsTemplate);
    }

    @JsonIgnore
    public Set<String> getAllPlaceholders() {
        Set<String> templateTags = new HashSet<>();
        templateTags.addAll(getPlaceholdersInTemplateVariantField(TemplateVariant::getSubject));
        templateTags.addAll(getPlaceholdersInTemplateVariantField(TemplateVariant::getEmailBody));
        templateTags.addAll(getPlaceholdersInTemplateVariantField(TemplateVariant::getSmsBody));
        return templateTags;
    }
}
