package ir.ac.kntu.repository;

import ir.ac.kntu.domain.ticket.Ticket;
import ir.ac.kntu.exception.ValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory repository tracking customer support tickets.
 */
public class TicketRepository {
    private final Map<String, Ticket> store = new ConcurrentHashMap<>();

    public synchronized void save(Ticket ticket) {
        if (ticket == null) {
            throw new ValidationException("Ticket cannot be null.");
        }
        store.put(ticket.getTicketId(), ticket);
    }

    public Optional<Ticket> findById(String ticketId) {
        if (ticketId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(ticketId.trim()));
    }

    public List<Ticket> findByPhone(String phone) {
        if (phone == null) {
            return List.of();
        }
        List<Ticket> list = new ArrayList<>();
        for (Ticket ticket : store.values()) {
            if (ticket.getUserPhoneNumber().equalsIgnoreCase(phone.trim())) {
                list.add(ticket);
            }
        }
        return Collections.unmodifiableList(list);
    }

    public List<Ticket> findAll() {
        return List.copyOf(store.values());
    }
}