package antifraud.dto;

import java.time.LocalDateTime;

public record TransactionOutDto(
        Long transactionId,
        Long amount,
        String ip,
        String number,
        String region,
        LocalDateTime date,
        String result,
        String feedback
) {
}
