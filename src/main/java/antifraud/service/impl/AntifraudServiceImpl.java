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
import antifraud.service.AntifraudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AntifraudServiceImpl implements AntifraudService {
    TransactionRepository transactionRepository;
    SuspiciousIpRepository suspiciousIPRepository;
    StolenCardRepository stolenCardRepository;
    AntifraudMapper mapper;
    TransactionLimitRepository transactionLimitRepository;
    TransactionLimit transactionLimit;

    @Autowired
    public AntifraudServiceImpl(TransactionRepository transactionRepository,
                                SuspiciousIpRepository suspiciousIPRepository,
                                StolenCardRepository stolenCardRepository,
                                AntifraudMapper mapper,
                                TransactionLimitRepository transactionLimitRepository,
                                @Qualifier("defaultMaxAllowed")
                                Long defaultMaxAllowed,
                                @Qualifier("defaultMaxManual")
                                Long defaultMaxManual) {
        this.transactionRepository = transactionRepository;
        this.suspiciousIPRepository = suspiciousIPRepository;
        this.stolenCardRepository = stolenCardRepository;
        this.mapper = mapper;
        this.transactionLimitRepository = transactionLimitRepository;
        transactionLimit = transactionLimitRepository.findById(1L)
                .orElseGet(() -> {
                    return transactionLimitRepository.save(new TransactionLimit(defaultMaxAllowed, defaultMaxManual));
                });
    }

    @Override
    public PostTransactionOutDto postTransaction(PostTransactionInDto postTransactionInDto) {
        String result;
        StringBuilder info = new StringBuilder();

        if (postTransactionInDto.amount() <= transactionLimit.getMaxAllowed()) {
            result = "ALLOWED";
            info.append("none");
        } else if (postTransactionInDto.amount() <= transactionLimit.getMaxManual()) {
            result = "MANUAL_PROCESSING";
            info.append("amount");
        } else {
            result = "PROHIBITED";
            info.append("amount");
        }

        if (stolenCardRepository.existsByNumber(postTransactionInDto.number())) {
            if (result.equals("PROHIBITED")) {
                info.append(", ");
            } else {
                result = "PROHIBITED";
                info.setLength(0);
            }
            info.append("card-number");
        }

        if (suspiciousIPRepository.existsByIp(postTransactionInDto.ip())) {
            if (result.equals("PROHIBITED")) {
                info.append(", ");
            } else {
                result = "PROHIBITED";
                info.setLength(0);
            }
            info.append("ip");
        }

        LocalDateTime oneHourAgo = postTransactionInDto.date().minusHours(1);
        Long countTransactionsDiffRegion = transactionRepository.
                countDistinctRegionsInPeriodExcludingCurrentRegion(oneHourAgo, postTransactionInDto.date(),
                        postTransactionInDto.region());

        if (countTransactionsDiffRegion > 2) {
            if (result.equals("PROHIBITED")) {
                info.append(", ");
            } else {
                result = "PROHIBITED";
                info.setLength(0);
            }
            info.append("region-correlation");
        } else if (countTransactionsDiffRegion == 2 && !result.equals("PROHIBITED")) {
            if (result.equals("MANUAL_PROCESSING")) {
                info.append(", ");
            } else {
                result = "MANUAL_PROCESSING";
                info.setLength(0);
            }
            info.append("region-correlation");
        }

        Long countTransactionUniqueDiffIp = transactionRepository
                .countDistinctIpsInPeriodExcludingCurrentIp(oneHourAgo, postTransactionInDto.date(),
                        postTransactionInDto.ip());

        if (countTransactionUniqueDiffIp > 2) {
            if (result.equals("PROHIBITED")) {
                info.append(", ");
            } else {
                result = "PROHIBITED";
                info.setLength(0);
            }
            info.append("ip-correlation");
        } else if (countTransactionUniqueDiffIp == 2 && !result.equals("PROHIBITED")) {
            if (result.equals("MANUAL_PROCESSING")) {
                info.append(", ");
            } else {
                result = "MANUAL_PROCESSING";
                info.setLength(0);
            }
            info.append("ip-correlation");
        }

        Transaction newTransaction = mapper.toTransaction(postTransactionInDto)
                .setResult(result)
                .setFeedback("");
        transactionRepository.save(newTransaction);
        return new PostTransactionOutDto(result, info.toString());
    }

    @Override
    public TransactionOutDto updateTransactionFeedback(UpdateTransactionFeedback feedback)
            throws TransactionNotFoundException,
            TransactionFeedbackAlreadyExistsException,
            TransactionFeedbackUnprocessableException {
        Transaction transaction = transactionRepository.findById(feedback.transactionId())
                .orElseThrow(TransactionNotFoundException::new);
        if (!transaction.getFeedback().isEmpty()) {
            throw new TransactionFeedbackAlreadyExistsException();
        }

        // if validity equals feedback: exception
        if (transaction.getResult().equals(feedback.feedback())) {
            throw new TransactionFeedbackUnprocessableException();
        }

        // save feedback into db
        transaction.setFeedback(feedback.feedback());
        transactionRepository.save(transaction);

        // adjust limits
        if (transaction.getResult().equals("ALLOWED")) {
            // dec maxAllowed
            transactionLimit.setMaxAllowed(updateLimit(transactionLimit.getMaxAllowed(),
                    transaction.getAmount(), false));
            if (feedback.feedback().equals("PROHIBITED")) {
                // dec maxManual
                transactionLimit.setMaxManual(updateLimit(transactionLimit.getMaxManual(),
                        transaction.getAmount(), false));
            }
        } else if (transaction.getResult().equals("MANUAL_PROCESSING")) {
            if (feedback.feedback().equals("ALLOWED")) {
                // inc maxAllowed
                transactionLimit.setMaxAllowed(updateLimit(transactionLimit.getMaxAllowed(),
                        transaction.getAmount(), true));
            } else {
                // dec maxManual
                transactionLimit.setMaxManual(updateLimit(transactionLimit.getMaxManual(),
                        transaction.getAmount(), false));
            }
        } else {
            // inc Manual
            transactionLimit.setMaxManual(updateLimit(transactionLimit.getMaxManual(),
                    transaction.getAmount(), true));
            if (feedback.feedback().equals("ALLOWED")) {
                // inc maxAllowed
                transactionLimit.setMaxAllowed(updateLimit(transactionLimit.getMaxAllowed(),
                        transaction.getAmount(), true));
            }
        }

        // save new limit
        transactionLimit = transactionLimitRepository.save(transactionLimit);
        return mapper.toDto(transaction);
    }

    private Long updateLimit(Long currentLimit, Long transactionValue, Boolean increase) {
        return (long) Math.ceil((0.8 * currentLimit +
                (increase ? 0.2 * transactionValue : -0.2 * transactionValue)));
    }

    @Override
    public TransactionOutDto[] getTransactionHistory() {
        List<Transaction> transactions = transactionRepository.findAllByOrderByIdAsc();
        return transactions.stream()
                .map(mapper::toDto)
                .toArray(TransactionOutDto[]::new);
    }

    @Override
    public TransactionOutDto[] getTransactionHistoryByNumber(String number)
            throws TransactionNotFoundException {
        List<Transaction> transactions = transactionRepository.findAllByNumberOrderByIdAsc(number);
        if (transactions.isEmpty()) {
            throw new TransactionNotFoundException();
        }
        return transactions.stream()
                .map(mapper::toDto)
                .toArray(TransactionOutDto[]::new);
    }

    @Override
    public SuspiciousIpOutDto postSuspiciousIp(SuspiciousIpInDto suspiciousIpInDto)
            throws SuspiciousIpExistsException {
        if (suspiciousIPRepository.existsByIp(suspiciousIpInDto.ip())) {
            throw new SuspiciousIpExistsException();
        }
        return mapper.toDto(suspiciousIPRepository.save(mapper.toSuspisiousIp(suspiciousIpInDto)));
    }

    @Override
    public void deleteSuspiciousIp(String ip) throws SuspiciousIpNotFoundException {
        SuspiciousIp suspiciousIp = suspiciousIPRepository.findByIp(ip)
                .orElseThrow(SuspiciousIpNotFoundException::new);
        suspiciousIPRepository.delete(suspiciousIp);
    }

    @Override
    public SuspiciousIpOutDto[] getSuspiciousIps() {
        List<SuspiciousIp> suspiciousIps = suspiciousIPRepository.findAllByOrderByIdAsc();
        return suspiciousIps.stream()
                .map(mapper::toDto)
                .toArray(SuspiciousIpOutDto[]::new);
    }

    @Override
    public StolenCardOutDto postStolenCard(StolenCardInDto stolenCardInDto) throws StolenCardExistsException {
        if (stolenCardRepository.existsByNumber(stolenCardInDto.number())) {
            throw new StolenCardExistsException();
        }
        return mapper.toDto(stolenCardRepository.save(mapper.toStolenCard(stolenCardInDto)));
    }

    @Override
    public void deleteStolenCard(String number) throws StolenCardNotFoundException {
        StolenCard stolenCard = stolenCardRepository.findByNumber(number)
                .orElseThrow(StolenCardNotFoundException::new);
        stolenCardRepository.delete(stolenCard);
    }

    @Override
    public StolenCardOutDto[] getStolenCards() {
        List<StolenCard> stolenCards = stolenCardRepository.findAllByOrderByIdAsc();
        return stolenCards.stream()
                .map(mapper::toDto)
                .toArray(StolenCardOutDto[]::new);
    }
}
