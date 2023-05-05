package org.codeforamerica.messaging.services;

import org.codeforamerica.messaging.models.Template;
import org.codeforamerica.messaging.repositories.TemplateRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class TemplateServiceTest {
    @Autowired
    TemplateService templateService;
    @MockBean
    TemplateRepository templateRepository;

    @Test
    void whenSmsMessageInputHasInvalidTemplateNameThenThrowRuntimeException() throws RuntimeException {
        String templateName = "not_a_template";

        Mockito.when(templateRepository.findFirstByName(templateName)).thenReturn(null);
        Assertions.assertThrows(RuntimeException.class, () -> templateService.buildBodyFromTemplate(templateName, new HashMap<>()));
    }

    @Test
    void whenSmsMessageInputHasValidTemplateParamsThenReturnFilledTemplate() throws RuntimeException {
        String templateName = "test";
        Map<String, Object> templateParams = Map.of("recipient_name", "Jarvis", "sender_name", "Papworth");
        Template template = Template.builder()
                .body("Merry Christmas {{recipient_name}}! From {{sender_name}}")
                .build();

        Mockito.when(templateRepository.findFirstByName(templateName)).thenReturn(template);
        Assertions.assertEquals("Merry Christmas Jarvis! From Papworth",
                templateService.buildBodyFromTemplate(templateName, templateParams));
    }

}
