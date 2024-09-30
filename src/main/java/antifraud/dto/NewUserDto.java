package antifraud.dto;

import jakarta.validation.constraints.NotBlank;

public record NewUserDto(
        @NotBlank
        String name,
        @NotBlank
        String username,
        @NotBlank
        String password
) {
}
