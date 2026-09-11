package ir.ac.kntu.service;

import ir.ac.kntu.domain.contact.Contact;
import ir.ac.kntu.domain.user.Customer;
import ir.ac.kntu.domain.user.KycStatus;
import ir.ac.kntu.exception.AccountNotFoundException;
import ir.ac.kntu.exception.ValidationException;
import ir.ac.kntu.repository.ContactRepository;
import ir.ac.kntu.repository.UserRepository;

import java.util.List;
import java.util.Objects;

/**
 * Service managing customer address books, contacts listing, and updates.
 */
public class ContactService {
    private final ContactRepository contactRepo;
    private final UserRepository userRepo;

    public ContactService(ContactRepository contactRepo, UserRepository userRepo) {
        this.contactRepo = Objects.requireNonNull(contactRepo, "Contact repo cannot be null.");
        this.userRepo = Objects.requireNonNull(userRepo, "User repo cannot be null.");
    }

    public synchronized void addContact(String userPhone, Contact contact) {
        Customer customer = getActiveCustomer(userPhone);
        ensureContactsEnabled(customer);
        contactRepo.saveContact(customer.getPhoneNumber(), contact);
    }

    public synchronized void updateContact(String userPhone, Contact contact) {
        Customer customer = getActiveCustomer(userPhone);
        ensureContactsEnabled(customer);
        contactRepo.updateContact(customer.getPhoneNumber(), contact);
    }

    public List<Contact> getContacts(String userPhone) {
        Customer customer = getActiveCustomer(userPhone);
        ensureContactsEnabled(customer);
        return contactRepo.getContacts(customer.getPhoneNumber());
    }

    public Contact getContact(String userPhone, String contactPhone) {
        Customer customer = getActiveCustomer(userPhone);
        ensureContactsEnabled(customer);
        return contactRepo.findContact(customer.getPhoneNumber(), contactPhone)
                .orElseThrow(() -> new ValidationException("Contact not found in address book."));
    }

    private Customer getActiveCustomer(String phone) {
        Customer customer = userRepo.findCustomerByPhone(phone)
                .orElseThrow(() -> new AccountNotFoundException("Customer not found: " + phone));

        if (customer.getKycStatus() != KycStatus.APPROVED) {
            throw new ValidationException("Access restricted: KYC approval required.");
        }
        return customer;
    }

    private void ensureContactsEnabled(Customer customer) {
        if (!customer.isContactsEnabled()) {
            throw new ValidationException("Contacts feature is currently disabled in your settings.");
        }
    }
}