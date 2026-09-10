package ir.ac.kntu.util;

import java.util.regex.Pattern;

/**
 * Utility class to enforce customer password complexity policies.
 */
public final class PasswordValidator {
    private static final Pattern HAS_UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern HAS_LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern HAS_DIGIT = Pattern.compile("[0-9]");
    private static final Pattern HAS_SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    private PasswordValidator() {
    }

    /**
     * Validates whether a password satisfies all complexity constraints.
     * Must contain uppercase, lowercase, numeric, and special characters.
     *
     * @param password the password candidate
     * @return true if valid, false otherwise
     */
    public static boolean isValid(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        return HAS_UPPERCASE.matcher(password).find()
                && HAS_LOWERCASE.matcher(password).find()
                && HAS_DIGIT.matcher(password).find()
                && HAS_SPECIAL.matcher(password).find();
    }
}