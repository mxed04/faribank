package ir.ac.kntu.domain.user;

import ir.ac.kntu.exception.ValidationException;

/**
 * Support administrator user with predetermined system privileges.
 */
public class SupportUser extends User {
    private final String username;

    public SupportUser(String firstName, String lastName, String username, String password) {
        super(firstName, lastName, password);
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Support username cannot be empty.");
        }
        this.username = username.trim();
    }

    public String getUsername() {
        return username;
    }
}