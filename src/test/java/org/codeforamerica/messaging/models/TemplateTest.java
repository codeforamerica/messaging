package org.codeforamerica.messaging.models;

import org.codeforamerica.messaging.repositories.TemplateRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class TemplateTest {
    @Autowired
    private TemplateRepository templateRepository;

    @Test
    void whenInputHasValidTemplateParamsThenReturnFilledTemplate() throws RuntimeException {
        Map<String, Object> templateParams = Map.of("recipient_name", "Jarvis", "sender_name", "Papworth");
        Template template = Template.builder()
                .subject("Hi {{recipient_name}}! First SMS ever!")
                .body("Merry Christmas {{recipient_name}}! From {{sender_name}}")
                .build();

        Assertions.assertEquals("Hi Jarvis! First SMS ever!",
                template.build(Template::getSubject, templateParams));
        Assertions.assertEquals("Merry Christmas Jarvis! From Papworth",
                template.build(Template::getBody, templateParams));
    }

    @Test
    @Transactional
    public void rejectsDuplicateNameWithMatchingLanguageAndVariant() {
        Template template = Template.builder()
                .name("test")
                .body("body")
                .build();
        Template template_duplicate = Template.builder()
                .name("test")
                .body("body")
                .build();
        templateRepository.save(template);
        assertThrows(DataIntegrityViolationException.class, () ->
                templateRepository.save(template_duplicate)
        );
        templateRepository.delete(template);
    }

    @Test
    @Transactional
    public void acceptsDuplicateNameWithDifferentLanguage() {
        Template template = Template.builder()
                .name("test")
                .body("body")
                .build();
        Template template_new_language = Template.builder()
                .name("test")
                .body("body")
                .language("es_ES")
                .build();
        templateRepository.save(template);
        templateRepository.save(template_new_language);
        templateRepository.delete(template);
        templateRepository.delete(template_new_language);
    }

    @Test
    @Transactional
    public void acceptsDuplicateNameWithDifferentVariant() {
        Template template = Template.builder()
                .name("test")
                .body("body")
                .build();
        Template template_new_variant = Template.builder()
                .name("test")
                .body("body")
                .variant("B")
                .build();
        templateRepository.save(template);
        templateRepository.save(template_new_variant);
        templateRepository.delete(template);
        templateRepository.delete(template_new_variant);
    }
}
