package ir.ac.kntu.service;

import ir.ac.kntu.domain.account.Transaction;
import ir.ac.kntu.domain.account.TransactionType;
import ir.ac.kntu.domain.user.Customer;
import ir.ac.kntu.exception.AccountNotFoundException;
import ir.ac.kntu.exception.ValidationException;
import ir.ac.kntu.repository.AccountRepository;
import ir.ac.kntu.repository.UserRepository;
import ir.ac.kntu.util.Calendar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceTest {
    private UserRepository userRepo;
    private AccountRepository accountRepo;
    private AuthService authService;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        userRepo = new UserRepository();
        accountRepo = new AccountRepository();
        authService = new AuthService(userRepo, accountRepo);
        accountService = new AccountService(accountRepo, userRepo);
    }

    @Test
    void testChargeAccountSuccessAndLedger() {
        Customer customer = new Customer("Arash", "Kamal", "09121113344", "1234567890", "Kamal@2024");
        authService.registerCustomer(customer);
        authService.approveKyc("09121113344");

        Transaction tx = accountService.chargeAccount("09121113344", 50000.0);
        assertNotNull(tx);
        assertEquals(TransactionType.CHARGE, tx.getType());
        assertEquals(50000.0, tx.getAmount());
        assertEquals(50000.0, accountService.getBalance("09121113344"));

        List<Transaction> txs = accountService.getTransactions("09121113344");
        assertEquals(1, txs.size());
        assertEquals(tx.getTrackingNumber(), txs.get(0).getTrackingNumber());
    }

    @Test
    void testChargeAccountRejectsInvalidAmounts() {
        Customer customer = new Customer("Sara", "Rad", "09122223344", "2345678901", "Sara@1234");
        authService.registerCustomer(customer);
        authService.approveKyc("09122223344");

        assertThrows(ValidationException.class, () -> accountService.chargeAccount("09122223344", 0.0));
        assertThrows(ValidationException.class, () -> accountService.chargeAccount("09122223344", -100.0));
    }

    @Test
    void testUnapprovedUserCannotAccessAccount() {
        Customer customer = new Customer("Amir", "Nouri", "09123334455", "3456789012", "Amir@5678");
        authService.registerCustomer(customer); // Status remains PENDING

        assertThrows(ValidationException.class, () -> accountService.chargeAccount("09123334455", 1000.0));
        assertThrows(ValidationException.class, () -> accountService.getBalance("09123334455"));
    }

    @Test
    void testNonExistingUserThrowsAccountNotFoundException() {
        assertThrows(AccountNotFoundException.class, () -> accountService.getBalance("09199999999"));
    }

    @Test
    void testFilterTransactionsByDateRange() {
        Customer customer = new Customer("Danial", "Azad", "09125556677", "4567890123", "Danial@9900");
        authService.registerCustomer(customer);
        authService.approveKyc("09125556677");

        Instant t0 = Calendar.now();
        Transaction tx1 = accountService.chargeAccount("09125556677", 10000.0);
        Transaction tx2 = accountService.chargeAccount("09125556677", 20000.0);

        List<Transaction> filtered = accountService.filterTransactions("09125556677", t0, Calendar.now().plusSeconds(300));
        assertEquals(2, filtered.size());

        // Test finding transaction by tracking ID
        Transaction found = accountService.findTransaction("09125556677", tx1.getTrackingNumber());
        assertEquals(tx1.getTrackingNumber(), found.getTrackingNumber());
    }
}