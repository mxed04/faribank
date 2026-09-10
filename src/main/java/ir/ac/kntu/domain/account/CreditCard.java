package ir.ac.kntu.domain.account;

import ir.ac.kntu.exception.ValidationException;

/**
 * Credit card linked to an approved customer bank account.
 */
public class CreditCard {
    private final String cardNumber;
    private String pin;

    public CreditCard(String cardNumber) {
        if (cardNumber == null || cardNumber.isBlank()) {
            throw new ValidationException("Card number cannot be empty.");
        }
        this.cardNumber = cardNumber.trim();
        this.pin = null;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        if (pin == null || !pin.matches("^\\d{4}$")) {
            throw new ValidationException("Card PIN must be a valid 4-digit code (0-9).");
        }
        this.pin = pin;
    }

    public boolean hasPin() {
        return pin != null && !pin.isEmpty();
    }
}