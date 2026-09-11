package ir.ac.kntu.service;

import ir.ac.kntu.domain.ticket.Ticket;
import ir.ac.kntu.domain.ticket.TicketSection;
import ir.ac.kntu.domain.ticket.TicketStatus;
import ir.ac.kntu.domain.user.Customer;
import ir.ac.kntu.exception.TicketNotFoundException;
import ir.ac.kntu.exception.ValidationException;
import ir.ac.kntu.repository.AccountRepository;
import ir.ac.kntu.repository.TicketRepository;
import ir.ac.kntu.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TicketServiceTest {
    private UserRepository userRepo;
    private TicketRepository ticketRepo;
    private AuthService authService;
    private TicketService ticketService;
    private Customer customer;

    @BeforeEach
    void setUp() {
        userRepo = new UserRepository();
        AccountRepository accountRepo = new AccountRepository();
        ticketRepo = new TicketRepository();
        authService = new AuthService(userRepo, accountRepo);
        ticketService = new TicketService(ticketRepo, userRepo);

        customer = new Customer("Milad", "Mohammadi", "09121118899", "1234567899", "Milad@2024");
        authService.registerCustomer(customer);
        authService.approveKyc(customer.getPhoneNumber());
    }

    @Test
    void testCreateAndRetrieveUserTickets() {
        Ticket t1 = ticketService.createTicket(
                customer.getPhoneNumber(), TicketSection.TRANSFER, "Transfer was delayed.");
        assertNotNull(t1);
        assertEquals(TicketStatus.REGISTERED, t1.getStatus());
        assertEquals("Transfer was delayed.", t1.getText());

        List<Ticket> tickets = ticketService.getUserTickets(customer.getPhoneNumber());
        assertEquals(1, tickets.size());
        assertEquals(t1.getTicketId(), tickets.get(0).getTicketId());
    }

    @Test
    void testOpenTicketsFilter() {
        Ticket t1 = ticketService.createTicket(
                customer.getPhoneNumber(), TicketSection.CONTACTS, "Cannot sync contact.");
        Ticket t2 = ticketService.createTicket(
                customer.getPhoneNumber(), TicketSection.SETTINGS, "Password issue.");

        ticketService.replyTicket(t2.getTicketId(), "Resolved and closed.", TicketStatus.CLOSED);

        List<Ticket> openTickets = ticketService.getOpenTickets(customer.getPhoneNumber());
        assertEquals(1, openTickets.size());
        assertEquals(t1.getTicketId(), openTickets.get(0).getTicketId());
    }

    @Test
    void testSupportReplyAndStatusUpdate() {
        Ticket ticket = ticketService.createTicket(
                customer.getPhoneNumber(), TicketSection.TRANSFER, "Double charge question.");

        ticketService.replyTicket(ticket.getTicketId(), "Refund processed.", TicketStatus.IN_PROGRESS);

        Ticket updated = ticketService.getTicket(ticket.getTicketId());
        assertEquals("Refund processed.", updated.getSupportReply());
        assertEquals(TicketStatus.IN_PROGRESS, updated.getStatus());
    }

    @Test
    void testFilterTicketsMultiCriteria() {
        ticketService.createTicket(customer.getPhoneNumber(), TicketSection.TRANSFER, "Query 1");
        ticketService.createTicket(customer.getPhoneNumber(), TicketSection.CONTACTS, "Query 2");

        List<Ticket> transfers = ticketService.filterTickets(
                TicketStatus.REGISTERED, TicketSection.TRANSFER, customer.getPhoneNumber());
        assertEquals(1, transfers.size());

        List<Ticket> contacts = ticketService.filterTickets(
                null, TicketSection.CONTACTS, null);
        assertEquals(1, contacts.size());
    }

    @Test
    void testNonExistingTicketThrowsException() {
        assertThrows(TicketNotFoundException.class, () ->
                ticketService.getTicket("TCK-999999"));
    }

    @Test
    void testEmptyTextThrowsValidationException() {
        assertThrows(ValidationException.class, () ->
                ticketService.createTicket(customer.getPhoneNumber(), TicketSection.SETTINGS, "   "));
    }
}