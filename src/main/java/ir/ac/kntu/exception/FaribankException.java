package ir.ac.kntu.exception;

/**
 * Root unchecked exception for all Faribank domain errors.
 */
public class FaribankException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public FaribankException(String message) {
        super(message);
    }

    public FaribankException(String message, Throwable cause) {
        super(message, cause);
    }
}