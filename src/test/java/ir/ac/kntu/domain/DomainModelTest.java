package ir.ac.kntu.domain;

import ir.ac.kntu.domain.account.Account;
import ir.ac.kntu.domain.account.CreditCard;
import ir.ac.kntu.domain.account.Transaction;
import ir.ac.kntu.domain.account.TransactionType;
import ir.ac.kntu.domain.contact.Contact;
import ir.ac.kntu.domain.ticket.Ticket;
import ir.ac.kntu.domain.ticket.TicketSection;
import ir.ac.kntu.domain.ticket.TicketStatus;
import ir.ac.kntu.domain.user.Customer;
import ir.ac.kntu.domain.user.KycStatus;
import ir.ac.kntu.exception.ValidationException;
import ir.ac.kntu.util.Calendar;
import ir.ac.kntu.util.PasswordValidator;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DomainModelTest {

    @Test
    void testPasswordValidatorRules() {
        assertTrue(PasswordValidator.isValid("StrongP@ss1"));
        assertFalse(PasswordValidator.isValid("alllowercase1@"));
        assertFalse(PasswordValidator.isValid("ALLUPPERCASE1@"));
        assertFalse(PasswordValidator.isValid("NoDigits@Pass"));
        assertFalse(PasswordValidator.isValid("NoSpecial1234"));
        assertFalse(PasswordValidator.isValid("Sh1@"));
    }

    @Test
    void testCustomerCreationAndKycFlow() {
        Customer customer = new Customer("Fariborz", "Danayi", "09123456789", "0123456789", "Fariborz@2024");
        assertEquals("Fariborz Danayi", customer.getFullName());
        assertEquals(KycStatus.PENDING, customer.getKycStatus());
        assertTrue(customer.isContactsFeatureEnabled());

        customer.setKycStatus(KycStatus.APPROVED);
        assertEquals(KycStatus.APPROVED, customer.getKycStatus());

        CreditCard card = new CreditCard("6037997123456789");
        card.setPin("1234");
        assertEquals("1234", card.getPin());

        Account account = new Account("AC1001", customer.getPhoneNumber(), card);
        customer.setAccount(account);
        assertEquals("AC1001", customer.getAccount().getAccountNumber());
    }

    @Test
    void testInvalidCustomerAttributesThrowValidationException() {
        assertThrows(ValidationException.class, () ->
                new Customer("Ali", "Reza", "12345", "0123456789", "Str0ng@Pass"));

        assertThrows(ValidationException.class, () ->
                new Customer("Ali", "Reza", "09121112233", "123", "Str0ng@Pass"));

        assertThrows(ValidationException.class, () ->
                new Customer("Ali", "Reza", "09121112233", "0123456789", "weak"));
    }

    @Test
    void testAccountTransactionsAndDescendingOrder() {
        CreditCard card = new CreditCard("6037123412341234");
        Account account = new Account("AC9999", "09120000000", card);

        Instant t1 = Calendar.now();
        Transaction tx1 = new Transaction("TX1", TransactionType.CHARGE, 1000.0, 0.0,
                null, account.getAccountNumber(), "Self", t1);
        account.charge(1000.0, tx1);
        assertEquals(1000.0, account.getBalance());

        // Explicitly set t2 after t1 to avoid zero-millisecond race condition in tests
        Instant t2 = t1.plusSeconds(60);
        Transaction tx2 = new Transaction("TX2", TransactionType.TRANSFER, 200.0, 1.0,
                account.getAccountNumber(), "AC8888", "Receiver", t2);
        account.debit(201.0, tx2);
        assertEquals(799.0, account.getBalance());

        // Verify descending order comparison
        assertTrue(tx2.compareTo(tx1) < 0);
        assertTrue(tx1.compareTo(tx2) > 0);
    }

    @Test
    void testContactAndTicketLifecycle() {
        Contact contact = new Contact("Sara", "Rad", "09128889900");
        assertEquals("Sara Rad", contact.getFullName());

        Instant now = Calendar.now();
        Ticket ticket = new Ticket("TCK1", "09128889900", TicketSection.TRANSFER, "Transfer issue", now);
        assertEquals(TicketStatus.REGISTERED, ticket.getStatus());

        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setSupportReply("Investigating");
        assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());
        assertEquals("Investigating", ticket.getSupportReply());
    }
}