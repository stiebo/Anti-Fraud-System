package antifraud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangeAccessDto(
        @NotBlank
        String username,
        @NotBlank
        @Pattern(regexp = "LOCK|UNLOCK")
        String operation
) {
}
