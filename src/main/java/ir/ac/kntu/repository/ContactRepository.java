package ir.ac.kntu.repository;

import ir.ac.kntu.domain.contact.Contact;
import ir.ac.kntu.exception.ValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory repository tracking customer address books and mutual ties.
 */
public class ContactRepository {
    private final Map<String, Map<String, Contact>> store = new ConcurrentHashMap<>();

    public synchronized void saveContact(String userPhone, Contact contact) {
        if (userPhone == null || contact == null) {
            throw new ValidationException("User phone and contact cannot be null.");
        }
        Map<String, Contact> userBook = store.computeIfAbsent(userPhone, k -> new ConcurrentHashMap<>());
        if (userBook.containsKey(contact.getPhoneNumber())) {
            throw new ValidationException("Contact phone number already exists.");
        }
        userBook.put(contact.getPhoneNumber(), contact);
    }

    public synchronized void updateContact(String userPhone, Contact contact) {
        if (userPhone == null || contact == null) {
            throw new ValidationException("User phone and contact cannot be null.");
        }
        Map<String, Contact> userBook = store.get(userPhone);
        if (userBook == null || !userBook.containsKey(contact.getPhoneNumber())) {
            throw new ValidationException("Contact not found to update.");
        }
        userBook.put(contact.getPhoneNumber(), contact);
    }

    public List<Contact> getContacts(String userPhone) {
        if (userPhone == null) {
            return List.of();
        }
        Map<String, Contact> userBook = store.get(userPhone);
        if (userBook == null) {
            return List.of();
        }
        return Collections.unmodifiableList(new ArrayList<>(userBook.values()));
    }

    public Optional<Contact> findContact(String userPhone, String contactPhone) {
        if (userPhone == null || contactPhone == null) {
            return Optional.empty();
        }
        Map<String, Contact> userBook = store.get(userPhone);
        if (userBook == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(userBook.get(contactPhone));
    }

    public boolean isMutual(String phoneA, String phoneB) {
        return findContact(phoneA, phoneB).isPresent()
                && findContact(phoneB, phoneA).isPresent();
    }
}