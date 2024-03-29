package org.codeforamerica.messaging.controllers;


import org.codeforamerica.messaging.TestData;
import org.codeforamerica.messaging.config.SecurityConfiguration;
import org.codeforamerica.messaging.models.MessageBatch;
import org.codeforamerica.messaging.models.MessageRequest;
import org.codeforamerica.messaging.services.MessageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Map;

import static org.codeforamerica.messaging.utils.CSVReader.LANGUAGE_HEADER;
import static org.codeforamerica.messaging.utils.CSVReader.TREATMENT_HEADER;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(MessageController.class)
@Import(SecurityConfiguration.class)
public class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private MessageService messageService;

    @Test
    public void getMessageUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/messages/" + TestData.BASE_ID))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser
    public void createMessageSuccessSmsOnly() throws Exception {
        String requestBody = """
                {
                    "toPhone": "%s",
                    "templateName": "%s"
                }
                """.formatted(TestData.TO_PHONE, TestData.TEMPLATE_NAME);

        Mockito.when(messageService.scheduleMessage(any()))
                .thenReturn(TestData.aMessage(TestData.aTemplateVariant().template(TestData.aTemplate().build()).build())
                        .id(TestData.BASE_ID)
                        .build());

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, endsWith("/messages/" + TestData.BASE_ID)));
    }

    @Test
    @WithMockUser
    public void createMessageSuccessEmailOnly() throws Exception {
        String requestBody = """
                {
                    "toEmail": "%s",
                    "templateName": "%s"
                }
                """.formatted(TestData.TO_EMAIL, TestData.TEMPLATE_NAME);

        Mockito.when(messageService.scheduleMessage(any()))
                .thenReturn(TestData.aMessage(TestData.aTemplateVariant().template(TestData.aTemplate().build()).build())
                        .id(TestData.BASE_ID)
                        .build());

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, endsWith("/messages/" + TestData.BASE_ID)));
    }

    @Test
    @WithMockUser
    public void createMessageSuccessMultiModal() throws Exception {
        String requestBody = """
                {
                    "toPhone": "%s",
                    "toEmail": "%s",
                    "templateName": "%s"
                }
                """.formatted(TestData.TO_PHONE, TestData.TO_EMAIL, TestData.TEMPLATE_NAME);

        Mockito.when(messageService.scheduleMessage(any()))
                .thenReturn(TestData.aMessage(TestData.aTemplateVariant().template(TestData.aTemplate().build()).build())
                        .id(TestData.BASE_ID)
                        .build());

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, endsWith("/messages/" + TestData.BASE_ID)));
    }

    @Test
    @WithMockUser
    public void createMessageEmptyPayload() throws Exception {
        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Mockito.verify(messageService, Mockito.never()).scheduleMessage(any());
    }

    @Test
    @WithMockUser
    public void createMessageBadPhoneNumber() throws Exception {
        String requestBody = """
                {
                    "toPhone": "A1234567890",
                    "templateName": "%s"
                }
                """.formatted(TestData.TEMPLATE_NAME);

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Mockito.verify(messageService, Mockito.never()).scheduleMessage(any());
    }

    @Test
    @WithMockUser
    public void createMessageBadEmail() throws Exception {
        String requestBody = """
                {
                    "toEmail": "not an email",
                    "templateName": "%s"
                }
                """.formatted(TestData.TO_EMAIL);

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Mockito.verify(messageService, Mockito.never()).scheduleMessage(any());
    }

    @Test
    @WithMockUser
    public void createMessageMissingPhoneAndEmail() throws Exception {
        String requestBody = """
                {
                    "templateName": "%s"
                }
                """.formatted(TestData.TEMPLATE_NAME);

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Mockito.verify(messageService, Mockito.never()).scheduleMessage(any());
    }

    @Test
    @WithMockUser
    public void whenExtraFieldsPresent_RejectsAsBadRequest() throws Exception {
        String requestBody = """
                {
                    "toEmail": "%s",
                    "templateName": "%s",
                    "invalid_field": "1234567890"
                }
                """.formatted(TestData.TO_EMAIL, TestData.TEMPLATE_NAME);

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        Mockito.verify(messageService, Mockito.never()).scheduleMessage(any());
    }

    @Test
    @WithMockUser
    public void whenTemplateParamsPresent_thenParseParams() throws Exception {
        String requestBody = """
                {
                    "toEmail": "%s",
                    "templateName": "%s",
                    "templateParams": {
                        "%s": "es",
                        "%s": "B"
                    }
                }
                """.formatted(TestData.TO_EMAIL, TestData.TEMPLATE_NAME, LANGUAGE_HEADER, TREATMENT_HEADER);

        MessageRequest expectedMessageRequest = TestData.aMessageRequest()
                .toEmail(TestData.TO_EMAIL)
                .templateParams(Map.of(
                        LANGUAGE_HEADER, "es",
                        TREATMENT_HEADER, "B"))
                .build();
        Mockito.when(messageService.scheduleMessage(expectedMessageRequest))
                .thenReturn(TestData.aMessage(TestData.aTemplateVariant().template(TestData.aTemplate().build()).build())
                        .id(TestData.BASE_ID)
                        .build());

        mockMvc.perform(post("/api/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, endsWith("/messages/" + TestData.BASE_ID)));
    }

    @Test
    @WithMockUser
    public void whenValidParametersForMessageBatch_ThenReturnsCreatedStatus() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );

        MessageBatch messageBatch = MessageBatch.builder().id(TestData.BASE_ID).build();
        Mockito.when(messageService.enqueueMessageBatch(any()))
                .thenReturn(messageBatch);

        mockMvc.perform(multipart("/api/v1/message_batches")
                                .file(file)
                                .param("templateName", "foo")
                                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string(HttpHeaders.LOCATION, endsWith("/message_batches/" + TestData.BASE_ID)));
    }
}
