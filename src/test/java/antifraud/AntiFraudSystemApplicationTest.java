package antifraud;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AntiFraudSystemApplicationTest {

    @Test
    void main_shouldStartApplicationWithoutException() {
        // call the main method with empty arguments; success is simply not throwing
        assertDoesNotThrow(() -> AntiFraudSystemApplication.main(new String[]{}));
    }
}
