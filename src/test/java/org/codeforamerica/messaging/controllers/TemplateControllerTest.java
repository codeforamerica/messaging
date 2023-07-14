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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

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
    void setup() throws Exception {
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
        Mockito.when(templateService.getActiveTemplateByName(TestData.TEMPLATE_NAME))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/templates/%s/%s".formatted(TestData.TEMPLATE_NAME, 1L)))
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
                                "emailBody":"English A Body: {{placeholder}}",
                                "smsBody":"English A Body: {{placeholder}}"
                            },
                            {
                                "language":"es",
                                "treatment":"B",
                                "subject":"Spanish B Subject: {{placeholder}}",
                                "emailBody":"Spanish B Body: {{placeholder}}",
                                "smsBody":"Spanish B Body: {{placeholder}}"
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
                            "emailBody":"English A Body: {{placeholder}}",
                            "smsBody":"English A Body: {{placeholder}}"
                        },
                        {
                            "language":"es",
                            "treatment":"B",
                            "subject":"Spanish B Subject: {{placeholder}}",
                            "emailBody":"Spanish B Body: {{placeholder}}",
                            "smsBody":"Spanish B Body: {{placeholder}}"
                        }
                    ]
                }
                """;

        Mockito.when(templateService.getTemplateByNameAndVersion(TEMPLATE_WITH_VARIANTS.getName(), TEMPLATE_WITH_VARIANTS.getVersion()))
                .thenReturn(Optional.of(TEMPLATE_WITH_VARIANTS));

        mockMvc.perform(get("/api/v1/templates/%s/%s".formatted(TEMPLATE_WITH_VARIANTS.getName(), TEMPLATE_WITH_VARIANTS.getVersion())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect((MockMvcResultMatchers.content().json(expectedResponse)));
    }

    @Test
    @WithMockUser
    public void whenCreatingTemplateWithVariants_thenReturnTemplateWithVariants() throws Exception {
        String requestBody = """
                {
                    "name":"Template name with variants",
                    "templateVariants":[
                        {
                            "subject":"English A Subject: {{placeholder}}",
                            "emailBody":"English A Body: {{placeholder}}",
                            "smsBody":"English A Body: {{placeholder}}"
                        },
                        {
                            "language":"es",
                            "treatment":"B",
                            "subject":"Spanish B Subject: {{placeholder}}",
                            "emailBody":"Spanish B Body: {{placeholder}}",
                            "smsBody":"Spanish B Body: {{placeholder}}"
                        }
                    ]
                }
                """;
        String expectedResponse = """
                {
                    "name":"Template name with variants",
                    "templateVariants":[
                        {
                            "language":"en",
                            "treatment":"A",
                            "subject":"English A Subject: {{placeholder}}",
                            "emailBody":"English A Body: {{placeholder}}",
                            "smsBody":"English A Body: {{placeholder}}"
                        },
                        {
                            "language":"es",
                            "treatment":"B",
                            "subject":"Spanish B Subject: {{placeholder}}",
                            "emailBody":"Spanish B Body: {{placeholder}}",
                            "smsBody":"Spanish B Body: {{placeholder}}"
                        }
                    ]
                }
                """;

        Mockito.when(templateService.createTemplate(TestData.aTemplate().id(null)
                        .name(TEMPLATE_NAME_WITH_VARIANTS)
                        .templateVariants(Set.of(TestData.aTemplateVariant().build(),
                                TestData.aTemplateVariant()
                                        .language("es")
                                        .treatment("B")
                                        .subject(TestData.TEMPLATE_SUBJECT_ES_B)
                                        .emailBody(TestData.TEMPLATE_BODY_ES_B)
                                        .smsBody(TestData.TEMPLATE_BODY_ES_B)
                                        .build()))
                        .build()))
                .thenReturn(TEMPLATE_WITH_VARIANTS);

        mockMvc.perform(post("/api/v1/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, endsWith("/templates/Template%20name%20with%20variants/1")))
                .andExpect(MockMvcResultMatchers.content().json(expectedResponse));
    }

    @Test
    @WithMockUser
    public void whenCreatingTemplateVariantsWithEmailBodyAndNoSubject_thenBadRequest() throws Exception {
        String requestBody = """
                {
                    "name":"Template name with variants",
                    "templateVariants":[
                        {
                            "language":"es",
                            "treatment":"B",
                            "emailBody":"Spanish B Body: {{placeholder}}"
                        }
                    ]
                }
                """;
        mockMvc.perform(post("/api/v1/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser
    public void whenModifyingATemplateVariantWithEmailBodyAndNoSubject_thenBadRequest() throws Exception {
        String requestBody = """
                {
                    "emailBody":"Spanish B Body: {{placeholder}}"
                }
                """;

        mockMvc.perform(put("/api/v1/templates/name/en/A")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

}
