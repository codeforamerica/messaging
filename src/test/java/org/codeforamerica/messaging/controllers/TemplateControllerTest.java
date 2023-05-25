package org.codeforamerica.messaging.controllers;


import org.codeforamerica.messaging.TestData;
import org.codeforamerica.messaging.config.SecurityConfiguration;
import org.codeforamerica.messaging.models.Template;
import org.codeforamerica.messaging.services.TemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(TemplateController.class)
@Import(SecurityConfiguration.class)
public class TemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private TemplateService templateService;

    private final String TEMPLATE_NAME_WITHOUT_VARIANTS = "Template name without variants";
    private final String TEMPLATE_NAME_WITH_VARIANTS = "Template name with variants";
    private final Template TEMPLATE_WITHOUT_VARIANTS = TestData.aTemplate()
            .name(TEMPLATE_NAME_WITHOUT_VARIANTS)
            .build();
    private final Template TEMPLATE_WITH_VARIANTS = TestData.aTemplate()
            .name(TEMPLATE_NAME_WITH_VARIANTS)
            .build();

    @BeforeEach
    void setup() {
        TestData.addVariantsToTemplate(TEMPLATE_WITH_VARIANTS);
    }

    @Test
    public void whenUnauthenticated_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/templates"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void whenAuthenticatedAndNoTemplates_thenNotFound() throws Exception {
        Mockito.when(templateService.getTemplateList())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/templates"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser
    public void whenAuthenticatedAndNoTemplatesWithMatchingName_thenNotFound() throws Exception {
        Mockito.when(templateService.getTemplateByName(TestData.TEMPLATE_NAME))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/templates/" + TestData.TEMPLATE_NAME))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser
    public void whenAuthenticatedAndTemplatesExist_thenReturnTemplatesAndVariants() throws Exception {
        String expectedResponse = """
                [
                    {
                        "name":"Template name with variants",
                        "templateVariants":[
                            {
                                "language":"en",
                                "treatment":"A",
                                "subject":"English A Subject: {{placeholder}}",
                                "body":"English A Body: {{placeholder}}"
                            },
                            {
                                "language":"es",
                                "treatment":"B",
                                "subject":"Spanish B Subject: {{placeholder}}",
                                "body":"Spanish B Body: {{placeholder}}"
                            }
                        ],
                    },
                    {
                        "name":"Template name without variants",
                        "templateVariants":[]
                    }
                ]
                """;

        Mockito.when(templateService.getTemplateList())
                .thenReturn(List.of(TEMPLATE_WITH_VARIANTS, TEMPLATE_WITHOUT_VARIANTS));

        mockMvc.perform(get("/api/v1/templates"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect((MockMvcResultMatchers.content().json(expectedResponse)));
    }

    @Test
    @WithMockUser
    public void whenAuthenticatedAndTemplateWithMatchingNameExists_thenReturnTemplateAndVariants() throws Exception {
        String expectedResponse = """
                {
                    "name":"Template name with variants",
                    "templateVariants":[
                        {
                            "language":"en",
                            "treatment":"A",
                            "subject":"English A Subject: {{placeholder}}",
                            "body":"English A Body: {{placeholder}}"
                        },
                        {
                            "language":"es",
                            "treatment":"B",
                            "subject":"Spanish B Subject: {{placeholder}}",
                            "body":"Spanish B Body: {{placeholder}}"
                        }
                    ]
                }
                """;

        Mockito.when(templateService.getTemplateByName(TEMPLATE_WITH_VARIANTS.getName()))
                .thenReturn(Optional.of(TEMPLATE_WITH_VARIANTS));

        mockMvc.perform(get("/api/v1/templates/" + TEMPLATE_WITH_VARIANTS.getName()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect((MockMvcResultMatchers.content().json(expectedResponse)));
    }

}
