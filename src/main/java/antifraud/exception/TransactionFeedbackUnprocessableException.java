package antifraud.exception;

public class TransactionFeedbackUnprocessableException extends RuntimeException{
    public TransactionFeedbackUnprocessableException() {
        super("Feedback and result cannot be the same");
    }
}
