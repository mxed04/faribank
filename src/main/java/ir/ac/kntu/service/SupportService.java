package ir.ac.kntu.service;

import ir.ac.kntu.domain.account.Account;
import ir.ac.kntu.domain.account.Transaction;
import ir.ac.kntu.domain.user.Customer;
import ir.ac.kntu.domain.user.CustomerSummary;
import ir.ac.kntu.exception.AccountNotFoundException;
import ir.ac.kntu.repository.AccountRepository;
import ir.ac.kntu.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Support service for searching users and inspecting restricted customer profiles.
 */
public class SupportService {
    private final UserRepository userRepo;
    private final AccountRepository accountRepo;

    public SupportService(UserRepository userRepo, AccountRepository accountRepo) {
        this.userRepo = Objects.requireNonNull(userRepo, "User repo cannot be null.");
        this.accountRepo = Objects.requireNonNull(accountRepo, "Account repo cannot be null.");
    }

    public List<CustomerSummary> searchCustomers(String phone, String first, String last) {
        List<Customer> all = userRepo.getAllCustomers();
        List<CustomerSummary> summaries = new ArrayList<>();

        for (Customer cust : all) {
            boolean matchPhone = phone == null || phone.isBlank()
                    || cust.getPhoneNumber().contains(phone.trim());
            boolean matchFirst = first == null || first.isBlank()
                    || cust.getFirstName().toLowerCase().contains(first.trim().toLowerCase());
            boolean matchLast = last == null || last.isBlank()
                    || cust.getLastName().toLowerCase().contains(last.trim().toLowerCase());

            if (matchPhone && matchFirst && matchLast) {
                summaries.add(toSummary(cust));
            }
        }
        return Collections.unmodifiableList(summaries);
    }

    public CustomerSummary getCustomerSummary(String phone) {
        Customer cust = userRepo.findCustomerByPhone(phone)
                .orElseThrow(() -> new AccountNotFoundException("User not found: " + phone));
        return toSummary(cust);
    }

    private CustomerSummary toSummary(Customer cust) {
        String accNum = "N/A";
        List<Transaction> txList = List.of();

        if (cust.getAccount() != null) {
            Account account = cust.getAccount();
            accNum = account.getAccountNumber();
            txList = account.getTransactions();
        } else {
            var optAcc = accountRepo.findByPhone(cust.getPhoneNumber());
            if (optAcc.isPresent()) {
                accNum = optAcc.get().getAccountNumber();
                txList = optAcc.get().getTransactions();
            }
        }

        return new CustomerSummary(
                cust.getFirstName(),
                cust.getLastName(),
                cust.getPhoneNumber(),
                accNum,
                txList
        );
    }
}