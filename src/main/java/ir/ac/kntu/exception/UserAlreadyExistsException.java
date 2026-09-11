package ir.ac.kntu.exception;

/**
 * Thrown when attempting to register a phone number or national code already present.
 */
public class UserAlreadyExistsException extends FaribankException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}