package ir.ac.kntu.domain.contact;

import ir.ac.kntu.exception.ValidationException;

import java.util.Objects;

/**
 * Address book contact linked to a customer account.
 */
public class Contact {
    private String firstName;
    private String lastName;
    private final String phoneNumber;

    public Contact(String firstName, String lastName, String phoneNumber) {
        setFirstName(firstName);
        setLastName(lastName);
        if (phoneNumber == null || !phoneNumber.matches("^09\\d{9}$")) {
            throw new ValidationException("Invalid contact phone number.");
        }
        this.phoneNumber = phoneNumber.trim();
    }

    public String getFirstName() {
        return firstName;
    }

    public final void setFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new ValidationException("Contact first name cannot be empty.");
        }
        this.firstName = firstName.trim();
    }

    public String getLastName() {
        return lastName;
    }

    public final void setLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new ValidationException("Contact last name cannot be empty.");
        }
        this.lastName = lastName.trim();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Contact contact)) {
            return false;
        }
        return phoneNumber.equals(contact.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneNumber);
    }
}