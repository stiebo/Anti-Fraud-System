package antifraud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangeRoleDto(
        @NotBlank
        String username,
        @NotBlank
        @Pattern(regexp = "MERCHANT|SUPPORT")
        String role
) {
}
