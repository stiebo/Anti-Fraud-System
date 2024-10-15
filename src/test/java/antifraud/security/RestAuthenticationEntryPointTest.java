package antifraud.security;

import antifraud.controller.AntifraudController;
import antifraud.service.AntifraudService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RestAuthenticationEntryPointTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testRequestWithoutUserAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/antifraud/history"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "INVALID")
    public void testRequestWithInvalidRoleReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/antifraud/history"))
                .andExpect(status().isForbidden());
    }

}