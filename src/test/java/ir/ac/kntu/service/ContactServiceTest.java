package ir.ac.kntu.service;

import ir.ac.kntu.domain.contact.Contact;
import ir.ac.kntu.domain.user.Customer;
import ir.ac.kntu.exception.ValidationException;
import ir.ac.kntu.repository.AccountRepository;
import ir.ac.kntu.repository.ContactRepository;
import ir.ac.kntu.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContactServiceTest {
    private UserRepository userRepo;
    private ContactRepository contactRepo;
    private AuthService authService;
    private ContactService contactService;
    private Customer owner;

    @BeforeEach
    void setUp() {
        userRepo = new UserRepository();
        AccountRepository accountRepo = new AccountRepository();
        contactRepo = new ContactRepository();
        authService = new AuthService(userRepo, accountRepo);
        contactService = new ContactService(contactRepo, userRepo);

        owner = new Customer("Farhad", "Majidi", "09121114455", "1112223334", "Farhad@1234");
        authService.registerCustomer(owner);
        authService.approveKyc(owner.getPhoneNumber());
    }

    @Test
    void testAddAndRetrieveContacts() {
        Contact contact = new Contact("Ali", "Daei", "09129990011");
        contactService.addContact(owner.getPhoneNumber(), contact);

        List<Contact> list = contactService.getContacts(owner.getPhoneNumber());
        assertEquals(1, list.size());
        assertEquals("Ali Daei", list.get(0).getFullName());

        Contact retrieved = contactService.getContact(owner.getPhoneNumber(), "09129990011");
        assertEquals("Ali", retrieved.getFirstName());
    }

    @Test
    void testAddDuplicateContactThrowsValidationException() {
        Contact c1 = new Contact("Ali", "Karimi", "09129990022");
        contactService.addContact(owner.getPhoneNumber(), c1);

        Contact c2 = new Contact("Ali", "Karimi Junior", "09129990022");
        assertThrows(ValidationException.class, () ->
                contactService.addContact(owner.getPhoneNumber(), c2));
    }

    @Test
    void testUpdateContactDetails() {
        Contact contact = new Contact("Mehdi", "Mahdavikia", "09129990033");
        contactService.addContact(owner.getPhoneNumber(), contact);

        contact.setLastName("Kia");
        contactService.updateContact(owner.getPhoneNumber(), contact);

        Contact updated = contactService.getContact(owner.getPhoneNumber(), "09129990033");
        assertEquals("Kia", updated.getLastName());
    }

    @Test
    void testDisabledContactsBlocksOperations() {
        owner.setContactsEnabled(false);

        Contact contact = new Contact("Javad", "Nekounam", "09129990044");
        assertThrows(ValidationException.class, () ->
                contactService.addContact(owner.getPhoneNumber(), contact));
        assertThrows(ValidationException.class, () ->
                contactService.getContacts(owner.getPhoneNumber()));
    }
}