package ir.ac.kntu.domain.user;

import ir.ac.kntu.domain.account.Transaction;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * DTO encapsulating restricted customer information for support operators.
 */
public class CustomerSummary {
    private final String firstName;
    private final String lastName;
    private final String phone;
    private final String accountNum;
    private final List<Transaction> transactions;

    public CustomerSummary(String firstName, String lastName, String phone,
                           String accountNum, List<Transaction> transactions) {
        this.firstName = Objects.requireNonNull(firstName, "First name is mandatory");
        this.lastName = Objects.requireNonNull(lastName, "Last name is mandatory");
        this.phone = Objects.requireNonNull(phone, "Phone is mandatory");
        this.accountNum = accountNum != null ? accountNum : "N/A";
        this.transactions = transactions != null
                ? Collections.unmodifiableList(transactions) : List.of();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAccountNum() {
        return accountNum;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}