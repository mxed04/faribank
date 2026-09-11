package ir.ac.kntu.service;

import ir.ac.kntu.domain.account.TransferReceipt;
import ir.ac.kntu.domain.contact.Contact;
import ir.ac.kntu.domain.user.Customer;
import ir.ac.kntu.exception.AccountNotFoundException;
import ir.ac.kntu.exception.InsufficientFundsException;
import ir.ac.kntu.exception.ValidationException;
import ir.ac.kntu.repository.AccountRepository;
import ir.ac.kntu.repository.ContactRepository;
import ir.ac.kntu.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransferServiceTest {
    private UserRepository userRepo;
    private AccountRepository accountRepo;
    private ContactRepository contactRepo;
    private AuthService authService;
    private AccountService accountService;
    private TransferService transferService;

    private Customer sender;
    private Customer receiver;

    @BeforeEach
    void setUp() {
        userRepo = new UserRepository();
        accountRepo = new AccountRepository();
        contactRepo = new ContactRepository();
        authService = new AuthService(userRepo, accountRepo);
        accountService = new AccountService(accountRepo, userRepo);
        transferService = new TransferService(accountRepo, userRepo, contactRepo);

        sender = new Customer("Ali", "Rad", "09121111111", "1111111111", "Pass@1234");
        receiver = new Customer("Reza", "Movahed", "09122222222", "2222222222", "Pass@5678");

        authService.registerCustomer(sender);
        authService.registerCustomer(receiver);

        authService.approveKyc(sender.getPhoneNumber());
        authService.approveKyc(receiver.getPhoneNumber());

        accountService.chargeAccount(sender.getPhoneNumber(), 10000.0);
    }

    @Test
    void testTransferByAccountSuccessAndFeeDeduction() {
        String destAcc = receiver.getAccount().getAccountNumber();
        TransferReceipt receipt = transferService.transferByAccount(sender.getPhoneNumber(), destAcc, 2000.0);

        assertNotNull(receipt);
        assertEquals(2000.0, receipt.getAmount());
        assertEquals(10.0, receipt.getFee()); // 0.5% fee
        assertEquals(2010.0, receipt.getTotalDeduction());

        // Balance assertions
        assertEquals(7990.0, accountService.getBalance(sender.getPhoneNumber()));
        assertEquals(2000.0, accountService.getBalance(receiver.getPhoneNumber()));

        // Recent accounts check
        assertTrue(sender.getRecentAccounts().contains(destAcc));
    }

    @Test
    void testTransferInsufficientBalanceThrowsException() {
        String destAcc = receiver.getAccount().getAccountNumber();
        assertThrows(InsufficientFundsException.class, () ->
                transferService.transferByAccount(sender.getPhoneNumber(), destAcc, 10000.0));
    }

    @Test
    void testSelfTransferThrowsValidationException() {
        String selfAcc = sender.getAccount().getAccountNumber();
        assertThrows(ValidationException.class, () ->
                transferService.transferByAccount(sender.getPhoneNumber(), selfAcc, 1000.0));
    }

    @Test
    void testMutualContactTransferFlow() {
        // Setup mutual contacts
        contactRepo.saveContact(sender.getPhoneNumber(), new Contact("Best", "Friend", receiver.getPhoneNumber()));
        contactRepo.saveContact(receiver.getPhoneNumber(), new Contact("Ali", "Partner", sender.getPhoneNumber()));

        TransferReceipt receipt = transferService.transferByContact(
                sender.getPhoneNumber(), receiver.getPhoneNumber(), 1000.0);

        assertNotNull(receipt);
        assertEquals("Best Friend", receipt.getDestOwnerName());
        assertEquals(1000.0, receipt.getAmount());
        assertEquals(5.0, receipt.getFee());
    }

    @Test
    void testNonMutualContactTransferRejected() {
        // Sender has receiver in contacts, but receiver has NOT saved sender
        contactRepo.saveContact(sender.getPhoneNumber(), new Contact("Friend", "One", receiver.getPhoneNumber()));

        assertThrows(ValidationException.class, () -> transferService.transferByContact(
                sender.getPhoneNumber(), receiver.getPhoneNumber(), 1000.0));
    }

    @Test
    void testTransferToContactDisabledRecipientThrowsException() {
        contactRepo.saveContact(sender.getPhoneNumber(), new Contact("Reza", "M", receiver.getPhoneNumber()));
        contactRepo.saveContact(receiver.getPhoneNumber(), new Contact("Ali", "R", sender.getPhoneNumber()));

        // Receiver disables contacts feature
        receiver.setContactsEnabled(false);

        assertThrows(ValidationException.class, () -> transferService.transferByContact(
                sender.getPhoneNumber(), receiver.getPhoneNumber(), 500.0));
    }

    @Test
    void testNonExistingAccountThrowsAccountNotFoundException() {
        assertThrows(AccountNotFoundException.class, () ->
                transferService.transferByAccount(sender.getPhoneNumber(), "NON_EXISTENT", 500.0));
    }
}