package antifraud.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SuspiciousIpOutDto(
        @Schema(example = "42")
        Long id,
        @Schema(example = "192.168.0.1")
        String ip
) {
}
