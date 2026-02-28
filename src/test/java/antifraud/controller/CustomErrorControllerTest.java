package antifraud.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.*;

class CustomErrorControllerTest {

    private CustomErrorController controller;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        controller = new CustomErrorController();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void handleError_withHtmlAcceptHeader_returnsForwardPath() {
        // arrange
        request.addHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE + ",application/json");

        // act
        String result = controller.handleError(request, response);

        // assert
        assertNotNull(result, "Result should not be null when HTML is accepted");
        assertEquals("forward:/error/error.html", result);
    }

    @Test
    void handleError_withNonHtmlAcceptHeader_returnsNull() {
        // arrange
        request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        // act
        String result = controller.handleError(request, response);

        // assert
        assertNull(result, "Result should be null when HTML is not accepted");
    }

    @Test
    void handleError_withNoAcceptHeader_returnsNull() {
        // no header added

        // act
        String result = controller.handleError(request, response);

        // assert
        assertNull(result, "Result should be null when Accept header is missing");
    }
}
