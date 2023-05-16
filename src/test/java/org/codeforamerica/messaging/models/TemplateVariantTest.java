package org.codeforamerica.messaging.models;

import org.codeforamerica.messaging.repositories.TemplateRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Map;

@SpringBootTest
public class TemplateVariantTest {
    @Autowired
    private TemplateRepository templateRepository;


    private final String SUBJECT = "Hi {{recipient_name}}! First SMS ever!";
    private final String BODY = "Merry Christmas {{recipient_name}}! From {{sender_name}}";
    private final Template TEMPLATE = Template.builder()
            .name("test")
            .build();
    private final TemplateVariant TEMPLATE_VARIANT = TemplateVariant.builder()
            .body(BODY)
            .subject(SUBJECT)
            .template(TEMPLATE)
            .build();

    @BeforeEach
    void setup() {
        templateRepository.save(TEMPLATE);
    }

    @AfterEach
    void tearDown() {
        templateRepository.deleteAll();
    }

    @Test
    void whenInputHasTemplateParams_thenTemplateUsesParams() throws IOException {
        Map<String, Object> templateParams = Map.of("recipient_name", "Jarvis", "sender_name", "Papworth");

        Assertions.assertEquals("Hi Jarvis! First SMS ever!",
                TEMPLATE_VARIANT.build(TemplateVariant::getSubject, templateParams));
        Assertions.assertEquals("Merry Christmas Jarvis! From Papworth",
                TEMPLATE_VARIANT.build(TemplateVariant::getBody, templateParams));
    }

}
