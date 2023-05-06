package org.codeforamerica.messaging.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class TemplateTest {

    @Test
    void whenInputHasValidTemplateParamsThenReturnFilledTemplate() throws RuntimeException {
        Map<String, Object> templateParams = Map.of("recipient_name", "Jarvis", "sender_name", "Papworth");
        Template template = Template.builder()
                .body("Merry Christmas {{recipient_name}}! From {{sender_name}}")
                .build();

        Assertions.assertEquals("Merry Christmas Jarvis! From Papworth",
                template.buildBodyFromTemplate(templateParams));
    }

}
