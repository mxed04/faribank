package ir.ac.kntu.service;

import ir.ac.kntu.domain.account.Account;
import ir.ac.kntu.domain.account.Transaction;
import ir.ac.kntu.domain.account.TransactionType;
import ir.ac.kntu.domain.account.TransferReceipt;
import ir.ac.kntu.domain.contact.Contact;
import ir.ac.kntu.domain.user.Customer;
import ir.ac.kntu.domain.user.KycStatus;
import ir.ac.kntu.exception.AccountNotFoundException;
import ir.ac.kntu.exception.InsufficientFundsException;
import ir.ac.kntu.exception.ValidationException;
import ir.ac.kntu.repository.AccountRepository;
import ir.ac.kntu.repository.ContactRepository;
import ir.ac.kntu.repository.UserRepository;
import ir.ac.kntu.util.Calendar;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Core fund transfer service supporting account, recent, and mutual contact transfers.
 */
public class TransferService {
    public static final double FEE_RATE = 0.005;
    private static final String ACC_ERR = "Destination account not found.";
    private static final long TX_PREFIX = 800000L;

    private final AccountRepository accountRepo;
    private final UserRepository userRepo;
    private final ContactRepository contactRepo;
    private final AtomicLong txCounter = new AtomicLong(TX_PREFIX);

    public TransferService(AccountRepository accountRepo, UserRepository userRepo,
                           ContactRepository contactRepo) {
        this.accountRepo = Objects.requireNonNull(accountRepo, "Account repo cannot be null.");
        this.userRepo = Objects.requireNonNull(userRepo, "User repo cannot be null.");
        this.contactRepo = Objects.requireNonNull(contactRepo, "Contact repo cannot be null.");
    }

    public TransferReceipt transferByAccount(String senderPhone, String destAccount, double amount) {
        if (amount <= 0) {
            throw new ValidationException("Transfer amount must be strictly positive.");
        }
        Customer sender = getApprovedCustomer(senderPhone);
        Account sourceAcc = sender.getAccount();

        Account destAcc = accountRepo.findByNumber(destAccount)
                .orElseThrow(() -> new AccountNotFoundException(ACC_ERR));

        Customer destOwner = userRepo.findCustomerByPhone(destAcc.getOwnerPhoneNumber())
                .orElseThrow(() -> new AccountNotFoundException(ACC_ERR));

        if (sourceAcc.getAccountNumber().equals(destAcc.getAccountNumber())) {
            throw new ValidationException("Cannot transfer funds to your own account.");
        }

        String displayName = resolveDisplayName(senderPhone, destOwner);
        return executeTransfer(sender, destOwner, amount, displayName);
    }

    public TransferReceipt transferByContact(String senderPhone, String contactPhone, double amount) {
        if (amount <= 0) {
            throw new ValidationException("Transfer amount must be strictly positive.");
        }
        Customer sender = getApprovedCustomer(senderPhone);
        if (!sender.isContactsEnabled()) {
            throw new ValidationException("Your contacts feature is disabled.");
        }

        Customer destOwner = userRepo.findCustomerByPhone(contactPhone)
                .orElseThrow(() -> new AccountNotFoundException("Contact user does not exist."));

        if (!destOwner.isContactsEnabled()) {
            throw new ValidationException("Recipient has disabled contact transfers.");
        }

        if (!contactRepo.isMutual(senderPhone, contactPhone)) {
            throw new ValidationException("Transfer allowed only if both users saved each other.");
        }

        Contact contact = contactRepo.findContact(senderPhone, contactPhone)
                .orElseThrow(() -> new ValidationException("Contact not found in address book."));

        return executeTransfer(sender, destOwner, amount, contact.getFullName());
    }

    public String resolveDisplayName(String senderPhone, Customer destOwner) {
        Optional<Contact> contact = contactRepo.findContact(senderPhone, destOwner.getPhoneNumber());
        if (contact.isPresent()) {
            return contact.get().getFullName();
        }
        return destOwner.getFullName();
    }

    public double calculateFee(double amount) {
        return amount * FEE_RATE;
    }

    private synchronized TransferReceipt executeTransfer(Customer sender, Customer destOwner,
                                                         double amount, String displayName) {
        Account sourceAcc = sender.getAccount();
        Account destAcc = destOwner.getAccount();

        if (destAcc == null) {
            throw new AccountNotFoundException(ACC_ERR);
        }

        double fee = calculateFee(amount);
        double total = amount + fee;

        if (sourceAcc.getBalance() < total) {
            throw new InsufficientFundsException("Insufficient funds: amount plus 0.5% fee exceeds balance.");
        }

        String trackId = "TR-" + txCounter.incrementAndGet();
        Instant timestamp = Calendar.now();

        Transaction debitTx = new Transaction(
                trackId,
                TransactionType.TRANSFER,
                amount,
                fee,
                sourceAcc.getAccountNumber(),
                destAcc.getAccountNumber(),
                displayName,
                timestamp
        );

        Transaction creditTx = new Transaction(
                trackId,
                TransactionType.TRANSFER,
                amount,
                0.0,
                sourceAcc.getAccountNumber(),
                destAcc.getAccountNumber(),
                sender.getFullName(),
                timestamp
        );

        sourceAcc.debit(total, debitTx);
        destAcc.credit(amount, creditTx);
        sender.addRecentAccount(destAcc.getAccountNumber());

        return new TransferReceipt(debitTx);
    }

    private Customer getApprovedCustomer(String phone) {
        Customer customer = userRepo.findCustomerByPhone(phone)
                .orElseThrow(() -> new AccountNotFoundException("Customer not found."));

        if (customer.getKycStatus() != KycStatus.APPROVED) {
            throw new ValidationException("Sender account inactive: KYC approval required.");
        }
        if (customer.getAccount() == null) {
            throw new AccountNotFoundException("Sender bank account not found.");
        }
        return customer;
    }
}