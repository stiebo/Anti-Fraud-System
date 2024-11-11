package antifraud.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ValidationErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        List<FieldError> fieldErrors,
        String path
) {
    public record FieldError(
            String fieldName,
            String errorMessage
    ){

    }
}
