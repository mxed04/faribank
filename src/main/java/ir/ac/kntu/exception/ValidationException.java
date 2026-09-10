package ir.ac.kntu.exception;

/**
 * Thrown when input data violates domain invariants or business constraints.
 */
public class ValidationException extends FaribankException {
    private static final long serialVersionUID = 1L;
    public ValidationException(String message) {
        super(message);
    }
}