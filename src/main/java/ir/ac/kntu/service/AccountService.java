package ir.ac.kntu.service;

import ir.ac.kntu.domain.account.Account;
import ir.ac.kntu.domain.account.Transaction;
import ir.ac.kntu.domain.account.TransactionType;
import ir.ac.kntu.domain.user.Customer;
import ir.ac.kntu.domain.user.KycStatus;
import ir.ac.kntu.exception.AccountNotFoundException;
import ir.ac.kntu.exception.ValidationException;
import ir.ac.kntu.repository.AccountRepository;
import ir.ac.kntu.repository.UserRepository;
import ir.ac.kntu.util.Calendar;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Handles account charging, balance inquiries, and filtered transaction ledgers.
 */
public class AccountService {
    private static final String ACC_ERR = "Account not found for user.";
    private static final long TX_PREFIX = 700000L;

    private final AccountRepository accountRepo;
    private final UserRepository userRepo;
    private final AtomicLong txCounter = new AtomicLong(TX_PREFIX);

    public AccountService(AccountRepository accountRepo, UserRepository userRepo) {
        this.accountRepo = Objects.requireNonNull(accountRepo, "Account repo cannot be null.");
        this.userRepo = Objects.requireNonNull(userRepo, "User repo cannot be null.");
    }

    public synchronized Transaction chargeAccount(String phone, double amount) {
        if (amount <= 0) {
            throw new ValidationException("Charge amount must be strictly positive.");
        }
        Account account = getApprovedAccount(phone);
        String trackId = "TX-" + txCounter.incrementAndGet();
        Instant timestamp = Calendar.now();

        Transaction chargeTx = new Transaction(
                trackId,
                TransactionType.CHARGE,
                amount,
                0.0,
                null,
                account.getAccountNumber(),
                "Self Deposit",
                timestamp
        );
        account.charge(amount, chargeTx);
        return chargeTx;
    }

    public double getBalance(String phone) {
        Account account = getApprovedAccount(phone);
        return account.getBalance();
    }

    public List<Transaction> getTransactions(String phone) {
        Account account = getApprovedAccount(phone);
        List<Transaction> list = new ArrayList<>(account.getTransactions());
        Collections.sort(list);
        return Collections.unmodifiableList(list);
    }

    public List<Transaction> filterTransactions(String phone, Instant startDate, Instant endDate) {
        List<Transaction> all = getTransactions(phone);
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction transaction : all) {
            Instant time = transaction.getTimestamp();
            boolean afterFrom = startDate == null || !time.isBefore(startDate);
            boolean beforeTo = endDate == null || !time.isAfter(endDate);
            if (afterFrom && beforeTo) {
                filtered.add(transaction);
            }
        }
        return Collections.unmodifiableList(filtered);
    }

    public Transaction findTransaction(String phone, String trackId) {
        Account account = getApprovedAccount(phone);
        for (Transaction transaction : account.getTransactions()) {
            if (transaction.getTrackingNumber().equalsIgnoreCase(trackId)) {
                return transaction;
            }
        }
        throw new ValidationException("Transaction not found: " + trackId);
    }

    public Account findAccountByNumber(String accNum) {
        if (accNum == null) {
            throw new ValidationException("Account number cannot be null.");
        }
        return accountRepo.findByNumber(accNum)
                .orElseThrow(() -> new AccountNotFoundException(ACC_ERR));
    }

    private Account getApprovedAccount(String phone) {
        Customer customer = userRepo.findCustomerByPhone(phone)
                .orElseThrow(() -> new AccountNotFoundException(ACC_ERR));

        if (customer.getKycStatus() != KycStatus.APPROVED) {
            throw new ValidationException("Account inactive: KYC approval required.");
        }

        return accountRepo.findByPhone(phone)
                .orElseThrow(() -> new AccountNotFoundException(ACC_ERR));
    }
}