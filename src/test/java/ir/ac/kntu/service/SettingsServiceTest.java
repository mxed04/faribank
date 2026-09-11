package ir.ac.kntu.service;

import ir.ac.kntu.domain.user.Customer;
import ir.ac.kntu.exception.ValidationException;
import ir.ac.kntu.repository.AccountRepository;
import ir.ac.kntu.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SettingsServiceTest {
    private UserRepository userRepo;
    private AuthService authService;
    private SettingsService settingsService;
    private Customer customer;

    @BeforeEach
    void setUp() {
        userRepo = new UserRepository();
        AccountRepository accountRepo = new AccountRepository();
        authService = new AuthService(userRepo, accountRepo);
        settingsService = new SettingsService(userRepo);

        customer = new Customer("Sina", "Talebi", "09123337788", "9988776655", "Sina@2024");
        authService.registerCustomer(customer);
        authService.approveKyc(customer.getPhoneNumber());
    }

    @Test
    void testChangePasswordSuccess() {
        settingsService.changePassword(customer.getPhoneNumber(), "Sina@2024", "NewStr0ng@Pass");
        assertEquals("NewStr0ng@Pass", customer.getPassword());
    }

    @Test
    void testChangePasswordWrongCurrentPasswordThrowsException() {
        assertThrows(ValidationException.class, () ->
                settingsService.changePassword(customer.getPhoneNumber(), "Wrong@Old", "NewStr0ng@Pass"));
    }

    @Test
    void testChangePasswordWeakPasswordThrowsException() {
        assertThrows(ValidationException.class, () ->
                settingsService.changePassword(customer.getPhoneNumber(), "Sina@2024", "weak"));
    }

    @Test
    void testSetCardPinSuccess() {
        settingsService.setCardPin(customer.getPhoneNumber(), "4321");
        assertEquals("4321", customer.getAccount().getCreditCard().getPin());
    }

    @Test
    void testSetCardPinInvalidLengthThrowsException() {
        assertThrows(ValidationException.class, () ->
                settingsService.setCardPin(customer.getPhoneNumber(), "123"));
        assertThrows(ValidationException.class, () ->
                settingsService.setCardPin(customer.getPhoneNumber(), "12345"));
        assertThrows(ValidationException.class, () ->
                settingsService.setCardPin(customer.getPhoneNumber(), "abcd"));
    }

    @Test
    void testToggleContactsFeature() {
        assertTrue(customer.isContactsEnabled());

        settingsService.toggleContacts(customer.getPhoneNumber(), false);
        assertFalse(customer.isContactsEnabled());

        settingsService.toggleContacts(customer.getPhoneNumber(), true);
        assertTrue(customer.isContactsEnabled());
    }
}