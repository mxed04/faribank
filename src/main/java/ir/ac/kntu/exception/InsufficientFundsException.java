package ir.ac.kntu.exception;

/**
 * Thrown when account balance is insufficient to cover amount plus fee.
 */
public class InsufficientFundsException extends FaribankException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}