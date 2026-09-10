package ir.ac.kntu.domain.ticket;

import ir.ac.kntu.exception.ValidationException;

import java.time.Instant;
import java.util.Objects;

/**
 * Support ticket domain entity for tracking user problems and support answers.
 */
public class Ticket {
    private final String id;
    private final String userPhoneNumber;
    private final TicketSection section;
    private final String text;
    private final Instant createdAt;
    private TicketStatus status;
    private String supportReply;

    public Ticket(String id, String userPhoneNumber, TicketSection section,
                  String text, Instant createdAt) {
        if (id == null || id.isBlank()) {
            throw new ValidationException("Ticket ID cannot be empty.");
        }
        if (userPhoneNumber == null || userPhoneNumber.isBlank()) {
            throw new ValidationException("User phone number cannot be empty.");
        }
        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException("Ticket text cannot be empty.");
        }
        this.id = id.trim();
        this.userPhoneNumber = userPhoneNumber.trim();
        this.section = Objects.requireNonNull(section, "Ticket section cannot be null.");
        this.text = text.trim();
        this.createdAt = Objects.requireNonNull(createdAt, "Created timestamp cannot be null.");
        this.status = TicketStatus.REGISTERED;
        this.supportReply = "";
    }

    public String getId() {
        return id;
    }

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public TicketSection getSection() {
        return section;
    }

    public String getText() {
        return text;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = Objects.requireNonNull(status, "Ticket status cannot be null.");
    }

    public String getSupportReply() {
        return supportReply;
    }

    public void setSupportReply(String supportReply) {
        this.supportReply = supportReply != null ? supportReply.trim() : "";
    }
}