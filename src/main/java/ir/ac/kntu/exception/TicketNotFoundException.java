package ir.ac.kntu.exception;

/**
 * Thrown when a requested support ticket cannot be located.
 */
public class TicketNotFoundException extends FaribankException {
    public TicketNotFoundException(String message) {
        super(message);
    }
}