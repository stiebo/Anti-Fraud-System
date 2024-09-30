package antifraud.dto;

import antifraud.validations.CardNumberConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;


public record PostTransactionInDto(
        @NotNull
        @Min(value = 1L)
        Long amount,
        @NotBlank
        @Pattern(regexp = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$")
        String ip,
        @NotBlank
        //@LuhnCheck  (For demo purposes, use a custom constraint.)
        @CardNumberConstraint
        String number,
        @NotBlank
        @Pattern(regexp = "EAP|ECA|HIC|LAC|MENA|SA|SSA")
        String region,
        @NotNull
        @DateTimeFormat(pattern = "yyyy-MM-ddTHH:mm:ss")
        LocalDateTime date
        ) {
}
