package antifraud.dto;

import antifraud.validations.CardNumberConstraint;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record StolenCardInDto(
        @NotBlank
        //@LuhnCheck  (For demo purposes, use a custom constraint.)
        @CardNumberConstraint
                @Schema(example = "4000008449433403")
        String number
) {
}
