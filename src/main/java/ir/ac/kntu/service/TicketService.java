package ir.ac.kntu.service;

import ir.ac.kntu.domain.ticket.Ticket;
import ir.ac.kntu.domain.ticket.TicketSection;
import ir.ac.kntu.domain.ticket.TicketStatus;
import ir.ac.kntu.domain.user.Customer;
import ir.ac.kntu.domain.user.KycStatus;
import ir.ac.kntu.exception.AccountNotFoundException;
import ir.ac.kntu.exception.TicketNotFoundException;
import ir.ac.kntu.exception.ValidationException;
import ir.ac.kntu.repository.TicketRepository;
import ir.ac.kntu.repository.UserRepository;
import ir.ac.kntu.util.Calendar;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service managing user tickets, support replies, and multi-criteria filters.
 */
public class TicketService {
    private static final String NOT_FOUND = "Ticket not found: ";
    private static final long TCK_PREFIX = 900000L;

    private final TicketRepository ticketRepo;
    private final UserRepository userRepo;
    private final AtomicLong counter = new AtomicLong(TCK_PREFIX);

    public TicketService(TicketRepository ticketRepo, UserRepository userRepo) {
        this.ticketRepo = Objects.requireNonNull(ticketRepo, "Ticket repo cannot be null.");
        this.userRepo = Objects.requireNonNull(userRepo, "User repo cannot be null.");
    }

    public synchronized Ticket createTicket(String phone, TicketSection section, String text) {
        ensureCustomerApproved(phone);
        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException("Ticket message cannot be empty.");
        }

        String ticketId = "TCK-" + counter.incrementAndGet();
        Instant createdAt = Calendar.now();
        Ticket ticket = new Ticket(ticketId, phone, section, text, createdAt);
        ticketRepo.save(ticket);
        return ticket;
    }

    public List<Ticket> getUserTickets(String phone) {
        ensureCustomerApproved(phone);
        return ticketRepo.findByPhone(phone);
    }

    public List<Ticket> getOpenTickets(String phone) {
        List<Ticket> all = getUserTickets(phone);
        List<Ticket> openList = new ArrayList<>();
        for (Ticket ticket : all) {
            if (ticket.getStatus() != TicketStatus.CLOSED) {
                openList.add(ticket);
            }
        }
        return Collections.unmodifiableList(openList);
    }

    public Ticket getTicket(String ticketId) {
        return ticketRepo.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(NOT_FOUND + ticketId));
    }

    public synchronized void replyTicket(String ticketId, String reply, TicketStatus status) {
        Ticket ticket = getTicket(ticketId);
        if (reply == null || reply.trim().isEmpty()) {
            throw new ValidationException("Reply message cannot be empty.");
        }
        ticket.setSupportReply(reply);
        ticket.setStatus(status);
    }

    public List<Ticket> filterTickets(TicketStatus status, TicketSection section, String phone) {
        List<Ticket> all = ticketRepo.findAll();
        List<Ticket> result = new ArrayList<>();

        for (Ticket ticket : all) {
            boolean matchesStatus = status == null || ticket.getStatus() == status;
            boolean matchesSec = section == null || ticket.getSection() == section;
            boolean matchesPhone = phone == null || phone.isBlank()
                    || ticket.getUserPhoneNumber().contains(phone.trim());

            if (matchesStatus && matchesSec && matchesPhone) {
                result.add(ticket);
            }
        }
        return Collections.unmodifiableList(result);
    }

    private void ensureCustomerApproved(String phone) {
        Customer customer = userRepo.findCustomerByPhone(phone)
                .orElseThrow(() -> new AccountNotFoundException("Customer not found: " + phone));

        if (customer.getKycStatus() != KycStatus.APPROVED) {
            throw new ValidationException("Access denied: KYC approval required.");
        }
    }
}