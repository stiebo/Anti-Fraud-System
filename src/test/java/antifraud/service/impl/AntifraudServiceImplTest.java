package antifraud.service.impl;

import antifraud.domain.StolenCard;
import antifraud.domain.SuspiciousIp;
import antifraud.domain.Transaction;
import antifraud.domain.TransactionLimit;
import antifraud.dto.*;
import antifraud.exception.*;
import antifraud.mapper.AntifraudMapper;
import antifraud.repository.StolenCardRepository;
import antifraud.repository.SuspiciousIpRepository;
import antifraud.repository.TransactionLimitRepository;
import antifraud.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Import(AntifraudMapper.class)
class AntifraudServiceImplTest {

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    SuspiciousIpRepository suspiciousIpRepository;

    @Mock
    StolenCardRepository stolenCardRepository;

    @Mock
    TransactionLimitRepository transactionLimitRepository;

    TransactionLimit transactionLimit;

    AntifraudServiceImpl antifraudService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transactionLimit = new TransactionLimit(200L, 1500L); // Example limits
        when(transactionLimitRepository.findById(1L)).thenReturn(Optional.of(transactionLimit));
        antifraudService = new AntifraudServiceImpl(transactionRepository, suspiciousIpRepository,
                stolenCardRepository, new AntifraudMapper(), transactionLimitRepository, 200L, 1500L);
    }

    private void testPostTransaction(Long amount, Boolean isStolenCard, Boolean isSuspiciousIp,
                                     Long distinctRegionsInPeriod, Long distinctIpsInPeriod,
                                     String expectedOutDtoResult, String expectedOutDtoInfo) {
        // Arrange
        PostTransactionInDto dtoIn = new PostTransactionInDto(amount, "192.168.1.1",
                "4000008449433403", "EAP", LocalDateTime.now());
        when(stolenCardRepository.existsByNumber(anyString())).thenReturn(isStolenCard);
        when(suspiciousIpRepository.existsByIp(anyString())).thenReturn(isSuspiciousIp);
        when(transactionRepository.countDistinctRegionsInPeriodExcludingCurrentRegion(any(), any(), anyString()))
                .thenReturn(distinctRegionsInPeriod);
        when(transactionRepository.countDistinctIpsInPeriodExcludingCurrentIp(any(), any(), anyString()))
                .thenReturn(distinctIpsInPeriod);

        // Act
        PostTransactionOutDto result = antifraudService.postTransaction(dtoIn);

        // Assert
        assertEquals(expectedOutDtoResult, result.result());
        assertEquals(expectedOutDtoInfo, result.info());
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(transactionCaptor.capture());
        Transaction savedTransaction = transactionCaptor.getValue();
        assertEquals(dtoIn.amount(), savedTransaction.getAmount());
        assertEquals(dtoIn.ip(), savedTransaction.getIp());
        assertEquals(dtoIn.number(), savedTransaction.getNumber());
        assertEquals(dtoIn.region(), savedTransaction.getRegion());
        assertEquals(dtoIn.date(), savedTransaction.getDate());
        assertEquals(result.result(), savedTransaction.getResult());
        assertEquals("", savedTransaction.getFeedback());
    }

    @Test
    void testConstructorAndTransactionLimitRepository1LNotFound() {
//        override BeforeSetup
        when(transactionLimitRepository.findById(1L)).thenReturn(Optional.empty());

        antifraudService = new AntifraudServiceImpl(transactionRepository, suspiciousIpRepository,
                stolenCardRepository, new AntifraudMapper(), transactionLimitRepository, 200L, 1500L);

        ArgumentCaptor<TransactionLimit> transactionLimitCaptor = ArgumentCaptor.forClass(TransactionLimit.class);
        verify(transactionLimitRepository, times(1)).save(transactionLimitCaptor.capture());
        TransactionLimit savedTransactionLimit = transactionLimitCaptor.getValue();
        assertEquals(200L, savedTransactionLimit.getMaxAllowed());
        assertEquals(1500L, savedTransactionLimit.getMaxManual());
    }

    @Test
    void testPostTransactionAllowed() {
        testPostTransaction(120L, false, false,
                0L, 0L,
                "ALLOWED", "none");
    }

    @Test
    void testPostTransactionManualProcessingAmount() {
        testPostTransaction(1200L, false, false,
                0L, 0L,
                "MANUAL_PROCESSING", "amount");
    }

    @Test
    void testPostTransactionProhibitedAmount() {
        testPostTransaction(12000L, false, false,
                0L, 0L,
                "PROHIBITED", "amount");
    }

    @Test
    void testPostTransactionProhibitedIsStolenCard() {
        testPostTransaction(120L, true, false,
                0L, 0L,
                "PROHIBITED", "card-number");
    }

    @Test
    void testPostTransactionProhibitedIsSuspiciousIp() {
        testPostTransaction(120L, false, true,
                0L, 0L,
                "PROHIBITED", "ip");
    }

    @Test
    void testPostTransactionProhibitedDistinctRegionsViolation() {
        testPostTransaction(120L, false, false,
                3L, 0L,
                "PROHIBITED", "region-correlation");
    }

    @Test
    void testPostTransactionManualProcessingDistinctRegionsViolation() {
        testPostTransaction(120L, false, false,
                2L, 0L,
                "MANUAL_PROCESSING", "region-correlation");
    }

    @Test
    void testPostTransactionProhibitedDistinctIpsViolation() {
        testPostTransaction(120L, false, false,
                0L, 3L,
                "PROHIBITED", "ip-correlation");
    }

    @Test
    void testPostTransactionManualProcessingDistinctIpsViolation() {
        testPostTransaction(120L, false, false,
                0L, 2L,
                "MANUAL_PROCESSING", "ip-correlation");
    }

    @Test
    void testPostTransactionManualProcessingAmountAndDistinctIpsViolation() {
        testPostTransaction(1200L, false, false,
                0L, 2L,
                "MANUAL_PROCESSING", "amount, ip-correlation");
    }

    @Test
    void testPostTransactionManualProcessingAmountDistinctRegionDistinctIpsViolation() {
        testPostTransaction(1200L, false, false,
                2L, 2L,
                "MANUAL_PROCESSING", "amount, region-correlation, ip-correlation");
    }

    @Test
    void testPostTransactionProhibitedAmountWithManualDistinctRegionDistinctIpsViolation() {
        testPostTransaction(12000L, false, false,
                2L, 2L,
                "PROHIBITED", "amount");
    }

    @Test
    void testPostTransactionProhibitedAmountStolenCardWithManualDistinctRegionDistinctIpsViolation() {
        testPostTransaction(12000L, true, false,
                2L, 2L,
                "PROHIBITED", "amount, card-number");
    }

    @Test
    void testPostTransactionProhibitedAmountStolenCardIpWithManualDistinctRegionDistinctIpsViolation() {
        testPostTransaction(12000L, true, true,
                2L, 2L,
                "PROHIBITED", "amount, card-number, ip");
    }

    @Test
    void testPostTransactionProhibitedAmountStolenCardIpDistinctRegionWithManualDistinctIpsViolation() {
        testPostTransaction(12000L, true, true,
                3L, 2L,
                "PROHIBITED", "amount, card-number, ip, region-correlation");
    }

    @Test
    void testPostTransactionProhibitedAmountStolenCardIpDistinctRegionIpsViolation() {
        testPostTransaction(12000L, true, true,
                3L, 3L,
                "PROHIBITED", "amount, card-number, ip, region-correlation," +
                        " ip-correlation");
    }

    @Test
    void testUpdateTransactionFeedbackTransactionNotFound() {
        // Arrange
        UpdateTransactionFeedback feedback = new UpdateTransactionFeedback(1L, "ALLOWED");
        when(transactionRepository.findById(feedback.transactionId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TransactionNotFoundException.class, () -> antifraudService.updateTransactionFeedback(feedback));
    }

    @Test
    void testUpdateTransactionFeedbackFeedbackAlreadyExists() {
        // Arrange
        UpdateTransactionFeedback feedback = new UpdateTransactionFeedback(1L, "ALLOWED");
        Transaction transaction = new Transaction()
                .setFeedback("PROHIBITED");
        when(transactionRepository.findById(feedback.transactionId())).thenReturn(Optional.of(transaction));

        // Act & Assert
        assertThrows(TransactionFeedbackAlreadyExistsException.class, () ->
                antifraudService.updateTransactionFeedback(feedback));
    }

    @Test
    void testUpdateTransactionFeedbackUnprocessableFeedback() {
        // Arrange
        UpdateTransactionFeedback feedback = new UpdateTransactionFeedback(1L, "ALLOWED");
        Transaction transaction = new Transaction()
                .setResult("ALLOWED")
                .setFeedback("");
        when(transactionRepository.findById(feedback.transactionId())).thenReturn(Optional.of(transaction));

        // Act & Assert
        assertThrows(TransactionFeedbackUnprocessableException.class, ()
                -> antifraudService.updateTransactionFeedback(feedback));
    }

    private void testUpdateTransactionWithFeedbackAndAdjustLimits(
            String transactionValidity, String transactionFeedback, Long transactionAmount,
            Long expectedNewMaxAllowed, Long expectedNewMaxManual) {
        // Arrange
        UpdateTransactionFeedback feedback = new UpdateTransactionFeedback(1L, transactionFeedback);
        Transaction transaction = new Transaction()
                .setId(1L)
                .setAmount(transactionAmount)
                .setResult(transactionValidity)
                .setFeedback("");
        when(transactionRepository.findById(feedback.transactionId())).thenReturn(Optional.of(transaction));

        // Act
        TransactionOutDto result = antifraudService.updateTransactionFeedback(feedback);

        // Assert
        // check outDto
        assertEquals(transaction.getId(), result.transactionId());
        assertEquals(transaction.getAmount(), result.amount());
        assertEquals(transaction.getResult(), result.result());
        assertEquals(transaction.getFeedback(), result.feedback());

        // check save updated transaction into repository
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(transactionCaptor.capture());
        Transaction savedTransaction = transactionCaptor.getValue();
        assertEquals(transaction.getAmount(), savedTransaction.getAmount());
        assertEquals(transaction.getResult(), savedTransaction.getResult());
        assertEquals(transaction.getFeedback(), savedTransaction.getFeedback());

        // check save updated transactionLimit into limitRepository
        ArgumentCaptor<TransactionLimit> transactionLimitCaptor = ArgumentCaptor.forClass(TransactionLimit.class);
        verify(transactionLimitRepository, times(1)).save(transactionLimitCaptor.capture());
        TransactionLimit savedTransactionLimit = transactionLimitCaptor.getValue();
        assertEquals(1L, savedTransactionLimit.getId());
        assertEquals(expectedNewMaxAllowed, savedTransactionLimit.getMaxAllowed());
        assertEquals(expectedNewMaxManual, savedTransactionLimit.getMaxManual());

    }

    @Test
    void testUpdateTransactionFeedbackValidityAllowedFeedbackManualDecreasesMaxAllowed() {
        testUpdateTransactionWithFeedbackAndAdjustLimits(
                "ALLOWED", "MANUAL_PROCESSING", 120L,
                136L, 1500L);
    }

    @Test
    void testUpdateTransactionFeedbackValidityAllowedFeedbackProhibitedDecreasesMaxAllowedAndMaxManual() {
        testUpdateTransactionWithFeedbackAndAdjustLimits(
                "ALLOWED", "PROHIBITED", 120L,
                136L, 1176L);
    }

    @Test
    void testUpdateTransactionFeedbackValidityManualFeedbackAllowedIncreasesMaxAllowed() {
        testUpdateTransactionWithFeedbackAndAdjustLimits(
                "MANUAL_PROCESSING", "ALLOWED", 250L,
                210L, 1500L);
    }

    @Test
    void testUpdateTransactionFeedbackValidityManualFeedbackProhibitedDecreasesMaxManual() {
        testUpdateTransactionWithFeedbackAndAdjustLimits(
                "MANUAL_PROCESSING", "PROHIBITED", 250L,
                200L, 1150L);
    }

    @Test
    void testUpdateTransactionFeedbackValidityProhibitedFeedbackAllowedIncreasesMaxAllowedAndMaxManual() {
        testUpdateTransactionWithFeedbackAndAdjustLimits(
                "PROHIBITED", "ALLOWED", 2500L,
                660L, 1700L);
    }

    @Test
    void testUpdateTransactionFeedbackValidityProhibitedFeedbackManualProcessingIncreasesMaxManual() {
        testUpdateTransactionWithFeedbackAndAdjustLimits(
                "PROHIBITED", "MANUAL_PROCESSING", 2500L,
                200L, 1700L);
    }

    @Test
    void testGetTransactionHistoryNonEmptyList() {
        // Arrange
        List<Transaction> transactions = new ArrayList<>();
        Transaction transaction1 = new Transaction()
                .setId(1L)
                .setAmount(100L)
                .setIp("192.168.0.1")
                .setNumber("4532015112830366")
                .setRegion("EAP")
                .setDate(LocalDateTime.now())
                .setResult("ALLOWED")
                .setFeedback("");
        transactions.add(transaction1);

        Transaction transaction2 = new Transaction()
                .setId(2L)
                .setAmount(200L)
                .setIp("192.168.0.2")
                .setNumber("4539578763621486")
                .setRegion("EUR")
                .setDate(LocalDateTime.now())
                .setResult("PROHIBITED")
                .setFeedback("ALLOWED");
        transactions.add(transaction2);

        when(transactionRepository.findAllByOrderByIdAsc()).thenReturn(transactions);

        // Act
        TransactionOutDto[] result = antifraudService.getTransactionHistory();

        // Assert
        assertEquals(2, result.length);
        for (int i = 0; i <= 1; i++) {
            assertEquals(transactions.get(i).getId(), result[i].transactionId());
            assertEquals(transactions.get(i).getAmount(), result[i].amount());
            assertEquals(transactions.get(i).getIp(), result[i].ip());
            assertEquals(transactions.get(i).getNumber(), result[i].number());
            assertEquals(transactions.get(i).getRegion(), result[i].region());
            assertEquals(transactions.get(i).getDate(), result[i].date());
            assertEquals(transactions.get(i).getResult(), result[i].result());
        }
    }

    @Test
    void testGetTransactionHistoryEmptyArray() {
        // Arrange
        when(transactionRepository.findAllByOrderByIdAsc()).thenReturn(new ArrayList<>());

        // Act
        TransactionOutDto[] result = antifraudService.getTransactionHistory();

        // Assert
        assertEquals(0, result.length);
    }

    @Test
    void testGetTransactionHistoryByNumberNonEmptyArrayValidNumber() {
        // Arrange
        List<Transaction> transactions = new ArrayList<>();
        Transaction transaction1 = new Transaction()
                .setId(1L)
                .setAmount(100L)
                .setIp("192.168.0.1")
                .setNumber("4532015112830366")
                .setRegion("EAP")
                .setDate(LocalDateTime.now())
                .setResult("ALLOWED")
                .setFeedback("");
        transactions.add(transaction1);

        Transaction transaction2 = new Transaction()
                .setId(2L)
                .setAmount(200L)
                .setIp("192.168.0.2")
                .setNumber("4532015112830366")
                .setRegion("EUR")
                .setDate(LocalDateTime.now())
                .setResult("PROHIBITED")
                .setFeedback("ALLOWED");
        transactions.add(transaction2);

        when(transactionRepository.findAllByNumberOrderByIdAsc("4532015112830366")).thenReturn(transactions);

        // Act
        TransactionOutDto[] result = antifraudService.getTransactionHistoryByNumber("4532015112830366");

        // Assert
        assertEquals(2, result.length);
        for (int i = 0; i <= 1; i++) {
            assertEquals(transactions.get(i).getId(), result[i].transactionId());
            assertEquals(transactions.get(i).getAmount(), result[i].amount());
            assertEquals(transactions.get(i).getIp(), result[i].ip());
            assertEquals(transactions.get(i).getNumber(), result[i].number());
            assertEquals(transactions.get(i).getRegion(), result[i].region());
            assertEquals(transactions.get(i).getDate(), result[i].date());
            assertEquals(transactions.get(i).getResult(), result[i].result());
        }
    }

    @Test
    void testGetTransactionHistoryByNumberEmptyListValidNumber() {
        // Arrange
        when(transactionRepository.findAllByNumberOrderByIdAsc("4532015112830366")).thenReturn(new ArrayList<>());

        // Act and Assert
        assertThrows(TransactionNotFoundException.class, ()
                -> antifraudService.getTransactionHistoryByNumber("4532015112830366"));

    }

    @Test
    void testPostSuspiciousIpThrowsException() {
        // Arrange
        SuspiciousIpInDto suspiciousIpInDto = new SuspiciousIpInDto("192.168.0.1");
        when(suspiciousIpRepository.existsByIp("192.168.0.1")).thenReturn(true);

        // Act and assert
        assertThrows(SuspiciousIpExistsException.class, ()
                -> antifraudService.postSuspiciousIp(suspiciousIpInDto));
    }

    @Test
    void testPostSuspiciousIpReturnsOutDto() {
        // Arrange
        SuspiciousIpInDto suspiciousIpInDto = new SuspiciousIpInDto("192.168.0.1");
        SuspiciousIp suspiciousIp = new SuspiciousIp().setId(1L).setIp("192.168.0.1");
        when(suspiciousIpRepository.existsByIp("192.168.0.1")).thenReturn(false);
        when(suspiciousIpRepository.save(any(SuspiciousIp.class))).thenReturn(suspiciousIp);

        // Act
        SuspiciousIpOutDto outDto = antifraudService.postSuspiciousIp(suspiciousIpInDto);

        // Assert
        assertEquals(suspiciousIp.getId(), outDto.id());
        assertEquals(suspiciousIp.getIp(), outDto.ip());

        ArgumentCaptor<SuspiciousIp> suspiciousIpCaptor = ArgumentCaptor.forClass(SuspiciousIp.class);
        verify(suspiciousIpRepository, times(1)).save(suspiciousIpCaptor.capture());
        SuspiciousIp savedSuspiciousIp = suspiciousIpCaptor.getValue();
        assertEquals(suspiciousIp.getIp(), savedSuspiciousIp.getIp());
    }

    @Test
    void testDeleteSuspiciousIpThrowsNotFoundException() {
        // Arrange
        when(suspiciousIpRepository.findByIp("192.168.0.1")).thenReturn(Optional.empty());

        // Act and assert
        assertThrows(SuspiciousIpNotFoundException.class, ()
                -> antifraudService.deleteSuspiciousIp("192.168.0.1"));
    }

    @Test
    void testDeleteSuspiciousIpWithIpFoundInRepository() {
        // Arrange
        String ip = "192.168.0.1";
        SuspiciousIp suspiciousIp = new SuspiciousIp().setId(1L).setIp("192.168.0.1");
        when(suspiciousIpRepository.findByIp("192.168.0.1")).thenReturn(Optional.of(suspiciousIp));

        // Act
        antifraudService.deleteSuspiciousIp(ip);

        // Assert
        verify(suspiciousIpRepository, times(1)).delete(suspiciousIp);
    }

    @Test
    void testGetSuspiciousIpsReturnsNonEmptyArray () {
        // Arrange
        List<SuspiciousIp> suspiciousIps = new ArrayList<>();
        suspiciousIps.add(new SuspiciousIp().setId(1L).setIp("192.168.0.1"));
        suspiciousIps.add(new SuspiciousIp().setId(2L).setIp("192.168.0.2"));

        when(suspiciousIpRepository.findAllByOrderByIdAsc()).thenReturn(suspiciousIps);

        // Act
        SuspiciousIpOutDto[] result = antifraudService.getSuspiciousIps();

        // Assert
        assertEquals(2, result.length);
        for (int i = 0; i <= 1; i++) {
            assertEquals(suspiciousIps.get(i).getId(), result[i].id());
            assertEquals(suspiciousIps.get(i).getIp(), result[i].ip());
        }
    }

    @Test
    void testGetSuspiciousIpsReturnsEmptyArray () {
        // Arrange
        when(suspiciousIpRepository.findAllByOrderByIdAsc()).thenReturn(new ArrayList<>());

        // Act
        SuspiciousIpOutDto[] result = antifraudService.getSuspiciousIps();

        // Assert
        assertEquals(0, result.length);
    }

    @Test
    void testPostStolenCardThrowsException() {
        // Arrange
        StolenCardInDto stolenCardInDto = new StolenCardInDto("4532015112830366");
        when(stolenCardRepository.existsByNumber("4532015112830366")).thenReturn(true);

        // Act and assert
        assertThrows(StolenCardExistsException.class, ()
                -> antifraudService.postStolenCard(stolenCardInDto));
    }

    @Test
    void testPostStolenCardReturnsOutDto() {
        // Arrange
        StolenCardInDto stolenCardInDto = new StolenCardInDto("4532015112830366");
        StolenCard stolenCard = new StolenCard().setId(1L).setNumber("4532015112830366");
        when(stolenCardRepository.existsByNumber("4532015112830366")).thenReturn(false);
        when(stolenCardRepository.save(any(StolenCard.class))).thenReturn(stolenCard);

        // Act
        StolenCardOutDto outDto = antifraudService.postStolenCard(stolenCardInDto);

        // Assert
        assertEquals(stolenCard.getId(), outDto.id());
        assertEquals(stolenCard.getNumber(), outDto.number());

        ArgumentCaptor<StolenCard> stolenCardCaptor = ArgumentCaptor.forClass(StolenCard.class);
        verify(stolenCardRepository, times(1)).save(stolenCardCaptor.capture());
        StolenCard savedStolenCard = stolenCardCaptor.getValue();
        assertEquals(stolenCard.getNumber(), savedStolenCard.getNumber());
    }

    @Test
    void testDeleteStolenCardThrowsNotFoundException() {
        // Arrange
        when(stolenCardRepository.findByNumber("4532015112830366")).thenReturn(Optional.empty());

        // Act and assert
        assertThrows(StolenCardNotFoundException.class, ()
                -> antifraudService.deleteStolenCard("4532015112830366"));
    }

    @Test
    void testDeleteStolenCardWithNumberFoundInRepository() {
        // Arrange
        String number = "4532015112830366";
        StolenCard stolenCard = new StolenCard().setId(1L).setNumber(number);
        when(stolenCardRepository.findByNumber(number)).thenReturn(Optional.of(stolenCard));

        // Act
        antifraudService.deleteStolenCard(number);

        // Assert
        verify(stolenCardRepository, times(1)).delete(stolenCard);
    }

    @Test
    void testGetStolenCardReturnsNonEmptyArray () {
        // Arrange
        List<StolenCard> stolenCards = new ArrayList<>();
        stolenCards.add(new StolenCard().setId(1L).setNumber("4532015112830366"));
        stolenCards.add(new StolenCard().setId(2L).setNumber("4539578763621486"));

        when(stolenCardRepository.findAllByOrderByIdAsc()).thenReturn(stolenCards);

        // Act
        StolenCardOutDto[] result = antifraudService.getStolenCards();
        // Assert
        assertEquals(2, result.length);
        for (int i = 0; i <= 1; i++) {
            assertEquals(stolenCards.get(i).getId(), result[i].id());
            assertEquals(stolenCards.get(i).getNumber(), result[i].number());
        }
    }

    @Test
    void testGetStolenCardsReturnsEmptyArray () {
        // Arrange
        when(stolenCardRepository.findAllByOrderByIdAsc()).thenReturn(new ArrayList<>());

        // Act
        StolenCardOutDto[] result = antifraudService.getStolenCards();

        // Assert
        assertEquals(0, result.length);
    }




}
