package ir.ac.kntu.service;

import ir.ac.kntu.domain.user.Customer;
import ir.ac.kntu.domain.user.CustomerSummary;
import ir.ac.kntu.repository.AccountRepository;
import ir.ac.kntu.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SupportServiceTest {
    private UserRepository userRepo;
    private AccountRepository accountRepo;
    private AuthService authService;
    private AccountService accountService;
    private SupportService supportService;

    @BeforeEach
    void setUp() {
        userRepo = new UserRepository();
        accountRepo = new AccountRepository();
        authService = new AuthService(userRepo, accountRepo);
        accountService = new AccountService(accountRepo, userRepo);
        supportService = new SupportService(userRepo, accountRepo);

        Customer c1 = new Customer("Mehdi", "Taremi", "09121113355", "1122334455", "Mehdi@9988");
        Customer c2 = new Customer("Sardar", "Azmoun", "09122224466", "2233445566", "Sardar@1234");
        Customer c3 = new Customer("Mehdi", "Ghayedi", "09123335577", "3344556677", "Mehdi@5678");

        authService.registerCustomer(c1);
        authService.registerCustomer(c2);
        authService.registerCustomer(c3);

        authService.approveKyc(c1.getPhoneNumber());
        authService.approveKyc(c2.getPhoneNumber());

        accountService.chargeAccount(c1.getPhoneNumber(), 5000.0);
    }

    @Test
    void testSearchByFirstName() {
        List<CustomerSummary> results = supportService.searchCustomers(null, "Mehdi", null);
        assertEquals(2, results.size());
    }

    @Test
    void testSearchByLastName() {
        List<CustomerSummary> results = supportService.searchCustomers(null, null, "Azmoun");
        assertEquals(1, results.size());
        assertEquals("Sardar Azmoun", results.get(0).getFullName());
    }

    @Test
    void testSearchByPhoneSubstring() {
        List<CustomerSummary> results = supportService.searchCustomers("1113355", null, null);
        assertEquals(1, results.size());
        assertEquals("Mehdi Taremi", results.get(0).getFullName());
    }

    @Test
    void testSearchByCombinedCriteria() {
        List<CustomerSummary> results = supportService.searchCustomers("09121113355", "Mehdi", "Taremi");
        assertEquals(1, results.size());
        assertEquals("Mehdi Taremi", results.get(0).getFullName());

        List<CustomerSummary> empty = supportService.searchCustomers("09121113355", "Mehdi", "Azmoun");
        assertTrue(empty.isEmpty());
    }

    @Test
    void testGetCustomerSummaryDetailsAndLedger() {
        CustomerSummary summary = supportService.getCustomerSummary("09121113355");
        assertNotNull(summary);
        assertEquals("Mehdi Taremi", summary.getFullName());
        assertNotEquals("N/A", summary.getAccountNum());
        assertEquals(1, summary.getTransactions().size());
        assertEquals(5000.0, summary.getTransactions().get(0).getAmount());
    }
}