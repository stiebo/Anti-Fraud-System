package antifraud.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = Mockito.mock(WebRequest.class);
        Mockito.when(request.getDescription(false)).thenReturn("uri=/test");
    }

    @Test
    void handleUnprocessableException_returnsUnprocessableEntityBody() {
        TransactionFeedbackUnprocessableException ex = new TransactionFeedbackUnprocessableException();

        ResponseEntity<ErrorResponse> response = handler.handleUnprocessableException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());

        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), body.status());
        assertEquals("Unprocessable entity", body.error());
        assertEquals(ex.getMessage(), body.message());
        assertEquals("uri=/test", body.path());
        assertNotNull(body.timestamp());
    }

    // additional tests to exercise other handlers (optional)
    @Test
    void handleBadRequestException_returnsBadRequestBody() {
        UnableToLockAdminException ex = new UnableToLockAdminException();
        ResponseEntity<ErrorResponse> response = handler.handleBadRequestException(ex, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.status());
        assertEquals("Bad request", body.error());
        assertEquals(ex.getMessage(), body.message());
        assertEquals("uri=/test", body.path());
    }

    @Test
    void handleInternalServerErrorException_returnsInternalServerErrorBody() {
        ClearDataErrorException ex = new ClearDataErrorException();
        ResponseEntity<ErrorResponse> response = handler.handleInternalServerErrorException(ex, request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), body.status());
        assertEquals("Internal Server Error", body.error());
        assertEquals(ex.getMessage(), body.message());
        assertEquals("uri=/test", body.path());
    }

    @Test
    void handleNotFoundException_returnsNotFoundBody() {
        UserNotFoundException ex = new UserNotFoundException();
        ResponseEntity<ErrorResponse> response = handler.handleNotFoundException(ex, request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertEquals(HttpStatus.NOT_FOUND.value(), body.status());
        assertEquals("Not Found", body.error());
        assertEquals(ex.getMessage(), body.message());
        assertEquals("uri=/test", body.path());
    }

    @Test
    void handleConflictException_returnsConflictBody() {
        UserExistsException ex = new UserExistsException();
        ResponseEntity<ErrorResponse> response = handler.handleConflictException(ex, request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertEquals(HttpStatus.CONFLICT.value(), body.status());
        assertEquals("Conflict", body.error());
        assertEquals(ex.getMessage(), body.message());
        assertEquals("uri=/test", body.path());
    }
}
