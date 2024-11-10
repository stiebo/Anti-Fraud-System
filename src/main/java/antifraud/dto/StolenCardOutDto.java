package antifraud.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record StolenCardOutDto(
        @Schema(example = "42")
        Long id,
        @Schema(example = "4000008449433403")
        String number
) {
}
