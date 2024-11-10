package antifraud.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record TransactionOutDto(
        @Schema(example = "42")
        Long transactionId,
        @Schema(example = "234")
        Long amount,
        @Schema(example = "192.168.1.1")
        String ip,
        @Schema(example = "4000008449433403")
        String number,
        @Schema(example = "MENA")
        String region,
        LocalDateTime date,
        @Schema(example = "MANUAL_PROCESSING")
        String result,
        @Schema(example = "")
        String feedback
) {
}
