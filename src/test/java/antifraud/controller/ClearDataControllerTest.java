package antifraud.controller;

import antifraud.repository.*;
import antifraud.security.RestAuthenticationEntryPoint;
import antifraud.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClearDataController.class)
@Import(SecurityConfig.class)
class ClearDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @MockBean
    private StolenCardRepository stolenCardRepository;

    @MockBean
    private SuspiciousIpRepository suspiciousIpRepository;

    @MockBean
    private TransactionLimitRepository transactionLimitRepository;

    @MockBean
    private TransactionRepository transactionRepository;

    @MockBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Reset the mock behavior before each test if necessary
        Mockito.reset(stolenCardRepository, suspiciousIpRepository, transactionLimitRepository,
                transactionRepository, userRepository);
    }

    @Test
    void clearData_success() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/clear-data")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":\"Demo server has been reset and all data removed.\"}"));

        // Verify each repository's deleteAll method is called once
        verify(stolenCardRepository, times(1)).deleteAll();
        verify(suspiciousIpRepository, times(1)).deleteAll();
        verify(transactionLimitRepository, times(1)).deleteAll();
        verify(transactionRepository, times(1)).deleteAll();
        verify(userRepository, times(1)).deleteAll();
    }

    @Test
    void clearData_exception() throws Exception {
        // Arrange: Simulate an exception in one of the repositories
        doThrow(new RuntimeException("Database error")).when(stolenCardRepository).deleteAll();

        // Act & Assert: Verify that an exception is thrown and handled
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/clear-data")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        // Verify only the first repository's deleteAll is called, others should not be called due to exception
        verify(stolenCardRepository, times(1)).deleteAll();
        verify(suspiciousIpRepository, times(0)).deleteAll();
        verify(transactionLimitRepository, times(0)).deleteAll();
        verify(transactionRepository, times(0)).deleteAll();
        verify(userRepository, times(0)).deleteAll();
    }
}