package antifraud.controller;

import antifraud.dto.*;
import antifraud.security.RestAuthenticationEntryPoint;
import antifraud.security.SecurityConfig;
import antifraud.service.AntiFraudService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AntiFraudController.class)
@Import(SecurityConfig.class)
class AntiFraudControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AntiFraudService antifraudService;

    @MockBean
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Autowired
    private ObjectMapper objectMapper;

//    @BeforeEach
//    public void setUp() {
//    }

    @Test
    @WithMockUser(roles = "MERCHANT")
    public void testPostTransactionReturnsTransactionOutDto() throws Exception {
        PostTransactionInDto inDto = new PostTransactionInDto(120L, "192.168.0.1", "4532015112830366", "EAP", LocalDateTime.now());
        when(antifraudService.postTransaction(any(PostTransactionInDto.class))).thenReturn(
                new PostTransactionOutDto("ALLOWED", "none"));

        mockMvc.perform(post("/api/antifraud/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("ALLOWED"))
                .andExpect(jsonPath("$.info").value("none"));
    }

    private void testPostTransactionInvalidData(PostTransactionInDto inDto) throws Exception {
        mockMvc.perform(post("/api/antifraud/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MERCHANT")
    public void testPostTransactionWithInvalidDataThrowsValidationException() throws Exception {
        final Long amount = 120L;
        final Long invalidAmount = 0L;
        final String ip = "192.168.0.1";
        final String invalidIp = "192168.0.1";
        final String validCardNumber = "4532015112830366";
        final String invalidCardNumber = "invalid";
        final String specialCharCardNumber = "*()+";
        final String wrongCardNumber = "4532015112830367";
        final String region = "EAP";
        final String invalidRegion = "AUT";
        final LocalDateTime now = LocalDateTime.now();

        testPostTransactionInvalidData(createInDto(invalidAmount, ip, validCardNumber, region, now));
        testPostTransactionInvalidData(createInDto(amount, invalidIp, validCardNumber, region, now));
        testPostTransactionInvalidData(createInDto(amount, ip, "", region, now));
        testPostTransactionInvalidData(createInDto(amount, ip, null, region, now));
        testPostTransactionInvalidData(createInDto(amount, ip, invalidCardNumber, region, now));
        testPostTransactionInvalidData(createInDto(amount, ip, specialCharCardNumber, region, now));
        testPostTransactionInvalidData(createInDto(amount, ip, wrongCardNumber, region, now));
        testPostTransactionInvalidData(createInDto(amount, ip, validCardNumber, invalidRegion, now));
        testPostTransactionInvalidData(createInDto(amount, ip, validCardNumber, region, null));
    }

    private PostTransactionInDto createInDto(Long amount, String ip, String number, String region, LocalDateTime date) {
        return new PostTransactionInDto(amount, ip, number, region, date);
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    public void testUploadTransactionFeedbackReturnsTransactionOutDto() throws Exception {
        TransactionOutDto outDto = new TransactionOutDto(1L, 120L, "192.168.0.1",
                "4532015112830366", "EAP", LocalDateTime.of(2024, 12, 12,
                10, 10), "ALLOWED", "ALLOWED");
        UpdateTransactionFeedback feedback = new UpdateTransactionFeedback(1L, "ALLOWED");
        when(antifraudService.updateTransactionFeedback(feedback)).thenReturn(outDto);

        mockMvc.perform(put("/api/antifraud/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedback)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(outDto.transactionId()))
                .andExpect(jsonPath("$.amount").value(outDto.amount()))
                .andExpect(jsonPath("$.ip").value(outDto.ip()))
                .andExpect(jsonPath("$.number").value(outDto.number()))
                .andExpect(jsonPath("$.region").value(outDto.region()))
                .andExpect(jsonPath("$.date").value("2024-12-12T10:10:00"))
                .andExpect(jsonPath("$.result").value(outDto.result()))
                .andExpect(jsonPath("$.feedback").value(outDto.feedback()));
    }

    private void testUploadTransactionFeedbackInvalidData(UpdateTransactionFeedback feedback) throws Exception {
        mockMvc.perform(put("/api/antifraud/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedback)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    public void testUploadTransactionFeedbackWithInvalidDataThrowsValidationException() throws Exception {
        testUploadTransactionFeedbackInvalidData(new UpdateTransactionFeedback(null, "ALLOWED"));
        testUploadTransactionFeedbackInvalidData(new UpdateTransactionFeedback(1L, "INVALID"));
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    public void testGetTransactionHistoryReturnsTransactionOutDtoArray() throws Exception {
        TransactionOutDto[] mockResponse =
                new TransactionOutDto[]{
                        new TransactionOutDto(1L, 120L, "192.168.0.1", "12345678909",
                                "EAP", LocalDateTime.of(2024, 12, 12, 10, 10),
                                "ALLOWED", ""),
                        new TransactionOutDto(2L, 12000L, "192.168.0.2", "123433238909",
                                "EAP", LocalDateTime.of(2024, 12, 12, 10, 10),
                                "ALLOWED", "PROHIBITED"),
                };
        when(antifraudService.getTransactionHistory()).thenReturn(mockResponse);

        ResultActions mockMvcResult = mockMvc.perform(get("/api/antifraud/history"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        for (int i = 0; i < mockResponse.length; i++) {
            mockMvcResult.andExpect(jsonPath("$[" + i + "].transactionId").value(mockResponse[i].transactionId()));
            mockMvcResult.andExpect(jsonPath("$[" + i + "].amount").value(mockResponse[i].amount()));
            mockMvcResult.andExpect(jsonPath("$[" + i + "].ip").value(mockResponse[i].ip()));
            mockMvcResult.andExpect(jsonPath("$[" + i + "].number").value(mockResponse[i].number()));
            mockMvcResult.andExpect(jsonPath("$[" + i + "].region").value(mockResponse[i].region()));
            mockMvcResult.andExpect(jsonPath("$[" + i + "].date").value("2024-12-12T10:10:00"));
            mockMvcResult.andExpect(jsonPath("$[" + i + "].result").value(mockResponse[i].result()));
            mockMvcResult.andExpect(jsonPath("$[" + i + "].feedback").value(mockResponse[i].feedback()));
        }
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    public void testGetTransactionHistoryReturnsEmptyArray() throws Exception {
        TransactionOutDto[] mockResponse = new TransactionOutDto[]{};
        when(antifraudService.getTransactionHistory()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/antifraud/history"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("[ ]"));

    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    public void testGetTransactionHistoryByNumberReturnsTransactionOutDtoArray() throws Exception {
        String number = "4532015112830366";
        TransactionOutDto[] mockResponse =
                new TransactionOutDto[]{
                        new TransactionOutDto(1L, 120L, "192.168.0.1", "4532015112830366",
                                "EAP", LocalDateTime.of(2024, 12, 12, 10, 10),
                                "ALLOWED", ""),
                        new TransactionOutDto(2L, 12000L, "192.168.0.2", "4532015112830366",
                                "EAP", LocalDateTime.of(2024, 12, 12, 10, 10),
                                "ALLOWED", "PROHIBITED"),
                };
        when(antifraudService.getTransactionHistoryByNumber(number)).thenReturn(mockResponse);

        ResultActions mockMvcResult = mockMvc.perform(get("/api/antifraud/history/" + number))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        for (int i = 0; i < mockResponse.length; i++) {
            mockMvcResult.andExpect(jsonPath("$[" + i + "].transactionId").value(mockResponse[i].transactionId()));
            mockMvcResult.andExpect(jsonPath("$[" + i + "].amount").value(mockResponse[i].amount()));
            mockMvcResult.andExpect(jsonPath("$[" + i + "].ip").value(mockResponse[i].ip()));
            mockMvcResult.andExpect(jsonPath("$[" + i + "].number").value(mockResponse[i].number()));
            mockMvcResult.andExpect(jsonPath("$[" + i + "].region").value(mockResponse[i].region()));
            mockMvcResult.andExpect(jsonPath("$[" + i + "].date").value("2024-12-12T10:10:00"));
            mockMvcResult.andExpect(jsonPath("$[" + i + "].result").value(mockResponse[i].result()));
            mockMvcResult.andExpect(jsonPath("$[" + i + "].feedback").value(mockResponse[i].feedback()));
        }
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    public void testGetTransactionHistoryByNumberReturnsEmptyArray() throws Exception {
        String number = "4532015112830366";
        TransactionOutDto[] mockResponse = new TransactionOutDto[]{};
        when(antifraudService.getTransactionHistoryByNumber(number)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/antifraud/history/" + number))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("[ ]"));
    }


    @Test
    @WithMockUser(roles = "SUPPORT")
    public void testGetTransactionHistoryByNumberWithCardNumberConstraintViolationReturnsBadRequest() throws Exception {
        String number = "INVALID NUMBER";

        ResultActions mockMvcResult = mockMvc.perform(get("/api/antifraud/history/" + number))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    public void testPostSuspiciousIpReturnsSuspiciousIpOutDto() throws Exception {
        SuspiciousIpInDto suspiciousIpInDto = new SuspiciousIpInDto("192.168.0.1");
        SuspiciousIpOutDto mockResponse = new SuspiciousIpOutDto(1L, suspiciousIpInDto.ip());

        when(antifraudService.postSuspiciousIp(suspiciousIpInDto)).thenReturn(mockResponse);

        mockMvc.perform(post("/api/antifraud/suspicious-ip")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(suspiciousIpInDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mockResponse.id()))
                .andExpect(jsonPath("$.ip").value(mockResponse.ip()));
    }

    private void testHelperPostSuspiciousIpWithInvalidInDtoReturnsBadRequest(String ip) throws Exception {
        SuspiciousIpInDto suspiciousIpInDto = new SuspiciousIpInDto(ip);

        mockMvc.perform(post("/api/antifraud/suspicious-ip")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(suspiciousIpInDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    public void testPostSuspiciousIpWithInvalidInDtoReturnsBadRequest() throws Exception {
        testHelperPostSuspiciousIpWithInvalidInDtoReturnsBadRequest("192.168.0.123456");
        testHelperPostSuspiciousIpWithInvalidInDtoReturnsBadRequest("");
        testHelperPostSuspiciousIpWithInvalidInDtoReturnsBadRequest("192.168.0");
        testHelperPostSuspiciousIpWithInvalidInDtoReturnsBadRequest("192.168.0.1.2");
        testHelperPostSuspiciousIpWithInvalidInDtoReturnsBadRequest("300.168.0.1");
        testHelperPostSuspiciousIpWithInvalidInDtoReturnsBadRequest(".168.0.1");
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    public void testDeleteSuspiciousIpReturnsStatusSuccess() throws Exception {
        String ip = "192.168.0.1";
        doNothing().when(antifraudService).deleteSuspiciousIp(ip);

        mockMvc.perform(delete("/api/antifraud/suspicious-ip/" + ip))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IP " + ip + " successfully removed!"));
        verify(antifraudService, times(1)).deleteSuspiciousIp(ip);
    }

    private void testHelperDeleteSuspiciousIpWithInvalidInputReturnsBadRequest(String ip) throws Exception {
        mockMvc.perform(delete("/api/antifraud/suspicious-ip/" + ip))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    public void testDeleteSuspiciousIpWithInvalidInputReturnsBadRequest() throws Exception {
        testHelperDeleteSuspiciousIpWithInvalidInputReturnsBadRequest("192.168.0.123456");
        testHelperDeleteSuspiciousIpWithInvalidInputReturnsBadRequest("192.168.0");
        testHelperDeleteSuspiciousIpWithInvalidInputReturnsBadRequest("192.168.0.1.2");
        testHelperDeleteSuspiciousIpWithInvalidInputReturnsBadRequest("300.168.0.1");
        testHelperDeleteSuspiciousIpWithInvalidInputReturnsBadRequest(".168.0.1");
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    public void testGetSuspiciousIpsReturnsSuspiciousIpsDtoArray() throws Exception {
        SuspiciousIpOutDto[] mockResponse = new SuspiciousIpOutDto[]{
                new SuspiciousIpOutDto(1L, "192.168.0.1"),
                new SuspiciousIpOutDto(2L, "192.168.0.2")
        };
        when(antifraudService.getSuspiciousIps()).thenReturn(mockResponse);

        ResultActions mockMvcResult = mockMvc.perform(get("/api/antifraud/suspicious-ip"))
                .andExpect(status().isOk());
        verify(antifraudService, times(1)).getSuspiciousIps();

        for (int i = 0; i < mockResponse.length; i++) {
            mockMvcResult.andExpect(jsonPath("$[" + i + "].id").value(mockResponse[i].id()));
            mockMvcResult.andExpect(jsonPath("$[" + i + "].ip").value(mockResponse[i].ip()));
        }
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    public void testGetSuspiciousIpsReturnsEmptyArray() throws Exception {
        when(antifraudService.getSuspiciousIps()).thenReturn(new SuspiciousIpOutDto[]{});

        mockMvc.perform(get("/api/antifraud/suspicious-ip"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("[ ]"));
        verify(antifraudService, times(1)).getSuspiciousIps();
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    public void testPostStolenCardReturnsStolenCardOutDto() throws Exception {
        StolenCardInDto stolenCardInDto = new StolenCardInDto("4532015112830366");
        StolenCardOutDto mockResponse = new StolenCardOutDto(1L, "4532015112830366");
        when(antifraudService.postStolenCard(stolenCardInDto)).thenReturn(mockResponse);

        mockMvc.perform(post("/api/antifraud/stolencard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stolenCardInDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mockResponse.id()))
                .andExpect(jsonPath("$.number").value(mockResponse.number()));
        verify(antifraudService, times(1)).postStolenCard(stolenCardInDto);
    }

    private void testHelperPostStolenCardWithInvalidDataReturnBadRequest(String number) throws Exception {
        StolenCardInDto stolenCardInDto = new StolenCardInDto(number);

        mockMvc.perform(post("/api/antifraud/stolencard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stolenCardInDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    public void testPostStolenCardWithInvalidDataReturnsBadRequest() throws Exception {
        testHelperPostStolenCardWithInvalidDataReturnBadRequest("Invalid number");
        testHelperPostStolenCardWithInvalidDataReturnBadRequest("4532015112830367");
        testHelperPostStolenCardWithInvalidDataReturnBadRequest("0532015112830366");
        testHelperPostStolenCardWithInvalidDataReturnBadRequest("45320151128303660");
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    public void testDeleteStolenCardReturnsSuccessfullyRemoved() throws Exception {
        String number = "4532015112830366";
        doNothing().when(antifraudService).deleteStolenCard(number);

        mockMvc.perform(delete("/api/antifraud/stolencard/" + number))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("Card %s successfully removed!".formatted(number)));
        verify(antifraudService, times(1)).deleteStolenCard(number);
    }

    private void testHelperDeleteStolenCardWithInvalidNumberReturnsBadRequest(String number) throws Exception {
        mockMvc.perform(delete("/api/antifraud/stolencard/" + number))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    public void testDeleteStolenCardWithInvalidNumbersReturnBadRequest() throws Exception {
        testHelperDeleteStolenCardWithInvalidNumberReturnsBadRequest("Invalid number");
        testHelperDeleteStolenCardWithInvalidNumberReturnsBadRequest("4532015112830367");
        testHelperDeleteStolenCardWithInvalidNumberReturnsBadRequest("0532015112830366");
        testHelperDeleteStolenCardWithInvalidNumberReturnsBadRequest("45320151128303660");
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    public void testDeleteStolenCardWithNumberMissingReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/antifraud/stolencard/"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    public void testGetStolenCardsReturnsStolenCardsOutDtoArray() throws Exception {
        StolenCardOutDto[] mockResponse = new StolenCardOutDto[]{
                new StolenCardOutDto(1L, "453201511283036"),
                new StolenCardOutDto(2L, "does not matter"),
                new StolenCardOutDto(3L, "I am a number"),
        };
        when(antifraudService.getStolenCards()).thenReturn(mockResponse);

        ResultActions mockMvcResult = mockMvc.perform(get("/api/antifraud/stolencard"))
                .andExpect(status().isOk());

        for (int i = 0; i < mockResponse.length; i++) {
            mockMvcResult.andExpect(jsonPath("$[" + i + "].id").value(mockResponse[i].id()));
            mockMvcResult.andExpect(jsonPath("$[" + i + "].number").value(mockResponse[i].number()));
        }
    }

    @Test
    @WithMockUser(roles = "SUPPORT")
    public void testGetStolenCardsReturnsEmptyArray() throws Exception {
        when(antifraudService.getStolenCards()).thenReturn(new StolenCardOutDto[]{});

        mockMvc.perform(get("/api/antifraud/stolencard"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("[ ]"));
        verify(antifraudService, times(1)).getStolenCards();
    }


}