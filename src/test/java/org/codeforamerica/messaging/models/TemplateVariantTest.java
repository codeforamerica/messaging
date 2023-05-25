package org.codeforamerica.messaging.models;

import org.codeforamerica.messaging.TestData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Map;

@SpringBootTest
public class TemplateVariantTest {
    @Test
    void whenInputHasTemplateParams_thenTemplateUsesParams() throws IOException {
        TemplateVariant templateVariant = TestData.aTemplateVariant().build();
        Map<String, Object> templateParams = Map.of("placeholder", "testing placeholder");

        Assertions.assertEquals("English A Subject: testing placeholder",
                templateVariant.build(TemplateVariant::getSubject, templateParams));
        Assertions.assertEquals("English A Body: testing placeholder",
                templateVariant.build(TemplateVariant::getBody, templateParams));
    }
}
