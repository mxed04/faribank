package ir.ac.kntu.exception;

/**
 * Thrown when an account cannot be located for the specified phone or account number.
 */
public class AccountNotFoundException extends FaribankException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}