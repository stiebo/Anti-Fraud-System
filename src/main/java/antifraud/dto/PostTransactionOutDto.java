package antifraud.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostTransactionOutDto(
        @Schema(example = "PROHIBITED")
        String result,
        @Schema(example = "amount, ip")
        String info
) {
}
