package org.codeforamerica.messaging.models;

import org.codeforamerica.messaging.TestData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class TemplateVariantTest {
    @Test
    void whenInputHasTemplateParams_thenTemplateUsesParams() {
        TemplateVariant templateVariant = TestData.aTemplateVariant().build();
        Map<String, String> templateParams = Map.of("placeholder", "testing placeholder");

        Assertions.assertEquals("English A Subject: testing placeholder",
                templateVariant.build(TemplateVariant::getSubject, templateParams));
        Assertions.assertEquals("English A Body: testing placeholder",
                templateVariant.build(TemplateVariant::getEmailBody, templateParams));
        Assertions.assertEquals("English A Body: testing placeholder",
                templateVariant.build(TemplateVariant::getSmsBody, templateParams));
    }
    @Test
    void whenInputHasNoTemplateParams_thenExceptionIsThrown() {
        TemplateVariant templateVariant = TestData.aTemplateVariant().build();
        Map<String, String> templateParams = Map.of("not placeholder", "testing placeholder");

        assertThrows(Exception.class, () -> templateVariant.build(TemplateVariant::getSubject, templateParams));
        assertThrows(Exception.class, () -> templateVariant.build(TemplateVariant::getEmailBody, templateParams));
        assertThrows(Exception.class, () -> templateVariant.build(TemplateVariant::getSmsBody, templateParams));
    }
}
