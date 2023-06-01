package org.codeforamerica.messaging.controllers;

import org.codeforamerica.messaging.TestData;
import org.codeforamerica.messaging.config.SecurityConfiguration;
import org.codeforamerica.messaging.services.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(MessageController.class)
@Import(SecurityConfiguration.class)
@TestPropertySource(properties = {"allowed-ip-addresses=::2,127.0.0.2"})
public class IpFilteringTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private MessageService messageService;

    @Test
    @WithMockUser
    public void whenInvalidIp_ThenNotAllowed() throws Exception {
        mockMvc.perform(get("/api/v1/messages/" + TestData.BASE_ID))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser
    public void whenValidIpForwardedForInPenultimatePosition_ThenAllowed() throws Exception {
        mockMvc.perform(get("/api/v1/messages/" + TestData.BASE_ID)
                .header("X-Forwarded-For", "127.0.0.2, 10.0.0.1"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser
    public void whenValidIpForwardedForInPenultimatePositionButExtraSpaces_ThenAllowed() throws Exception {
        mockMvc.perform(get("/api/v1/messages/" + TestData.BASE_ID)
                        .header("X-Forwarded-For", " 127.0.0.2,   10.0.0.1"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser
    public void whenInValidIpForwardedForInPenultimatePosition_ThenForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/messages/" + TestData.BASE_ID)
                        .header("X-Forwarded-For", "127.0.0.1, 10.0.0.1"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser
    public void whenValidIpForwardedForNotInPenultimatePosition_ThenForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/messages/" + TestData.BASE_ID)
                        .header("X-Forwarded-For", "127.0.0.2"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

}
