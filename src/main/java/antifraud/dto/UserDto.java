package antifraud.dto;

public record UserDto(
        Long id,
        String name,
        String username,
        String role,
        String status
) {
}
