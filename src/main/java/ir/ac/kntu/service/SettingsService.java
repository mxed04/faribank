package ir.ac.kntu.service;

import ir.ac.kntu.domain.account.Account;
import ir.ac.kntu.domain.account.CreditCard;
import ir.ac.kntu.domain.user.Customer;
import ir.ac.kntu.domain.user.KycStatus;
import ir.ac.kntu.exception.AccountNotFoundException;
import ir.ac.kntu.exception.ValidationException;
import ir.ac.kntu.repository.UserRepository;
import ir.ac.kntu.util.PasswordValidator;

import java.util.Objects;

/**
 * Service managing user credentials, credit card PIN, and feature toggles.
 */
public class SettingsService {
    private final UserRepository userRepo;

    public SettingsService(UserRepository userRepo) {
        this.userRepo = Objects.requireNonNull(userRepo, "User repo cannot be null.");
    }

    public synchronized void changePassword(String userPhone, String currPass, String newPass) {
        Customer customer = getCustomer(userPhone);

        if (!customer.getPassword().equals(currPass)) {
            throw new ValidationException("Current password does not match.");
        }
        if (!PasswordValidator.isValid(newPass)) {
            throw new ValidationException("Weak password: uppercase, lowercase, digit, and special char required.");
        }
        customer.setPassword(newPass);
    }

    public synchronized void setCardPin(String userPhone, String cardPin) {
        Customer customer = getCustomer(userPhone);

        if (customer.getKycStatus() != KycStatus.APPROVED) {
            throw new ValidationException("KYC approval required to set card PIN.");
        }
        Account account = customer.getAccount();
        if (account == null) {
            throw new AccountNotFoundException("Bank account not found for customer.");
        }

        CreditCard card = account.getCreditCard();
        if (card == null) {
            throw new ValidationException("Credit card not found for account.");
        }
        card.setPin(cardPin);
    }

    public synchronized void toggleContacts(String userPhone, boolean enabled) {
        Customer customer = getCustomer(userPhone);
        customer.setContactsEnabled(enabled);
    }

    private Customer getCustomer(String phone) {
        return userRepo.findCustomerByPhone(phone)
                .orElseThrow(() -> new AccountNotFoundException("Customer not found: " + phone));
    }
}