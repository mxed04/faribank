package ir.ac.kntu.service;

import ir.ac.kntu.domain.user.Customer;
import ir.ac.kntu.domain.user.KycStatus;
import ir.ac.kntu.domain.user.SupportUser;
import ir.ac.kntu.exception.AuthenticationException;
import ir.ac.kntu.exception.UserAlreadyExistsException;
import ir.ac.kntu.repository.AccountRepository;
import ir.ac.kntu.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {
    private UserRepository userRepo;
    private AccountRepository accountRepo;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepo = new UserRepository();
        accountRepo = new AccountRepository();
        authService = new AuthService(userRepo, accountRepo);
    }

    @Test
    void testCustomerRegistrationAndDuplicateRejections() {
        Customer customer = new Customer("Ali", "Rad", "09121112233", "0011223344", "Pass@1234");
        Customer saved = authService.registerCustomer(customer);
        assertNotNull(saved);
        assertEquals(KycStatus.PENDING, saved.getKycStatus());

        Customer dupPhone = new Customer("Sara", "Moein", "09121112233", "9988776655", "Pass@1234");
        assertThrows(UserAlreadyExistsException.class, () -> authService.registerCustomer(dupPhone));

        Customer dupNid = new Customer("Sara", "Moein", "09129998877", "0011223344", "Pass@1234");
        assertThrows(UserAlreadyExistsException.class, () -> authService.registerCustomer(dupNid));
    }

    @Test
    void testCustomerAuthenticationFlow() {
        Customer customer = new Customer("Reza", "Karimi", "09123334455", "1122334455", "Secret@2024");
        authService.registerCustomer(customer);

        Customer authenticated = authService.authenticateCustomer("09123334455", "Secret@2024");
        assertEquals("Reza Karimi", authenticated.getFullName());

        assertThrows(AuthenticationException.class, () ->
                authService.authenticateCustomer("09123334455", "WrongPassword@1"));

        assertThrows(AuthenticationException.class, () ->
                authService.authenticateCustomer("09120000000", "Secret@2024"));
    }

    @Test
    void testSupportUserDefaultAuthentication() {
        SupportUser admin = authService.authenticateSupport("admin", "Admin@1234");
        assertEquals("Admin Support", admin.getFullName());

        assertThrows(AuthenticationException.class, () ->
                authService.authenticateSupport("admin", "WrongPass@1"));
    }

    @Test
    void testKycApprovalGeneratesAccountAndCard() {
        Customer customer = new Customer("Mehdi", "Farid", "09124445566", "2233445566", "Mehdi@9988");
        authService.registerCustomer(customer);
        assertEquals(1, authService.getPendingKycRequests().size());

        authService.approveKyc("09124445566");

        Customer authenticated = authService.authenticateCustomer("09124445566", "Mehdi@9988");
        assertEquals(KycStatus.APPROVED, authenticated.getKycStatus());
        assertNotNull(authenticated.getAccount());
        assertNotNull(authenticated.getAccount().getCreditCard());
        assertTrue(accountRepo.exists(authenticated.getAccount().getAccountNumber()));
        assertEquals(0, authService.getPendingKycRequests().size());
    }

    @Test
    void testKycRejectionAndResubmission() {
        Customer customer = new Customer("Nima", "Ahmadi", "09125556677", "3344556677", "Nima@4321");
        authService.registerCustomer(customer);

        authService.rejectKyc("09125556677", "National code does not match ID document.");

        Customer authenticated = authService.authenticateCustomer("09125556677", "Nima@4321");
        assertEquals(KycStatus.REJECTED, authenticated.getKycStatus());
        assertEquals("National code does not match ID document.", authenticated.getRejectionReason());

        authService.updateCustomerKycData("09125556677", "Nima", "Ahmadi", "4455667788");
        assertEquals(KycStatus.PENDING, authenticated.getKycStatus());
        assertEquals("", authenticated.getRejectionReason());
    }
}