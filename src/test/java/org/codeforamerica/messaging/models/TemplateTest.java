package org.codeforamerica.messaging.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class TemplateTest {

    @Test
    void whenInputHasValidTemplateParamsThenReturnFilledTemplate() throws RuntimeException {
        Map<String, Object> templateParams = Map.of("recipient_name", "Jarvis", "sender_name", "Papworth");
        Template template = Template.builder()
                .subject("Hi {{recipient_name}}! First SMS ever!")
                .body("Merry Christmas {{recipient_name}}! From {{sender_name}}")
                .build();

        Assertions.assertEquals("Hi Jarvis! First SMS ever!",
                template.buildSubjectFromTemplate(templateParams));
        Assertions.assertEquals("Merry Christmas Jarvis! From Papworth",
                template.buildBodyFromTemplate(templateParams));
    }

}
