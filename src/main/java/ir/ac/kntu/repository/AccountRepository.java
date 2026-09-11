package ir.ac.kntu.repository;

import ir.ac.kntu.domain.account.Account;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory repository tracking active bank accounts by account number.
 */
public class AccountRepository {
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    private final Map<String, Account> accountsByPhone = new ConcurrentHashMap<>();

    public synchronized void save(Account account) {
        accounts.put(account.getAccountNumber(), account);
        accountsByPhone.put(account.getOwnerPhoneNumber(), account);
    }

    public Optional<Account> findByNumber(String accNum) {
        if (accNum == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(accounts.get(accNum.trim()));
    }

    public Optional<Account> findByPhone(String phone) {
        if (phone == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(accountsByPhone.get(phone.trim()));
    }

    public boolean exists(String accNum) {
        return accNum != null && accounts.containsKey(accNum.trim());
    }
}