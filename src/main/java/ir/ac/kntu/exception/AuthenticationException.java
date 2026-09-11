package ir.ac.kntu.exception;

/**
 * Thrown when credentials fail to match any active system user.
 */
public class AuthenticationException extends FaribankException {
    public AuthenticationException(String message) {
        super(message);
    }
}