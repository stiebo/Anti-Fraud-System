package antifraud.service;

import antifraud.dto.*;

public interface AntifraudService {
    PostTransactionOutDto postTransaction(PostTransactionInDto postTransactionInDto);

    TransactionOutDto updateTransactionFeedback(UpdateTransactionFeedback feedback);

    TransactionOutDto[] getTransactionHistory();

    TransactionOutDto[] getTransactionHistoryByNumber(String number);

    SuspiciousIpOutDto postSuspiciousIp(SuspiciousIpInDto suspiciousIpInDto);

    void deleteSuspiciousIp(String ip);

    SuspiciousIpOutDto[] getSuspiciousIps();

    StolenCardOutDto postStolenCard(StolenCardInDto stolenCardInDto);

    void deleteStolenCard(String number);

    StolenCardOutDto[] getStolenCards();
}
