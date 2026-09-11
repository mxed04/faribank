package ir.ac.kntu.service;

import ir.ac.kntu.domain.account.Account;
import ir.ac.kntu.domain.account.CreditCard;
import ir.ac.kntu.domain.user.Customer;
import ir.ac.kntu.domain.user.KycStatus;
import ir.ac.kntu.domain.user.SupportUser;
import ir.ac.kntu.exception.AuthenticationException;
import ir.ac.kntu.exception.UserAlreadyExistsException;
import ir.ac.kntu.exception.ValidationException;
import ir.ac.kntu.repository.AccountRepository;
import ir.ac.kntu.repository.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Handles registration, authentication, and customer KYC lifecycle management.
 */
public class AuthService {
    private static final String AUTH_ERR = "Invalid phone number or password.";
    private static final long CARD_PREFIX = 6037990000000000L;
    private static final long ACC_PREFIX = 100000L;

    private final UserRepository userRepo;
    private final AccountRepository accountRepo;
    private final AtomicLong accountCounter = new AtomicLong(ACC_PREFIX);
    private final AtomicLong cardCounter = new AtomicLong(CARD_PREFIX);

    public AuthService(UserRepository userRepo, AccountRepository accountRepo) {
        this.userRepo = Objects.requireNonNull(userRepo, "User repository cannot be null.");
        this.accountRepo = Objects.requireNonNull(accountRepo, "Account repository cannot be null.");
    }

    public synchronized Customer registerCustomer(Customer customer) {
        if (customer == null) {
            throw new ValidationException("Customer cannot be null.");
        }
        if (userRepo.findCustomerByPhone(customer.getPhoneNumber()).isPresent()) {
            throw new UserAlreadyExistsException("Phone number already exists in Faribank system.");
        }
        if (userRepo.findCustomerByNationalCode(customer.getNationalCode()).isPresent()) {
            throw new UserAlreadyExistsException("National code already exists in Faribank system.");
        }

        userRepo.saveCustomer(customer);
        return customer;
    }

    public Customer authenticateCustomer(String phone, String password) {
        Customer customer = userRepo.findCustomerByPhone(phone)
                .orElseThrow(() -> new AuthenticationException(AUTH_ERR));

        if (!customer.getPassword().equals(password)) {
            throw new AuthenticationException(AUTH_ERR);
        }
        return customer;
    }

    public SupportUser authenticateSupport(String username, String password) {
        SupportUser support = userRepo.findSupportByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Invalid support username or password."));

        if (!support.getPassword().equals(password)) {
            throw new AuthenticationException("Invalid support username or password.");
        }
        return support;
    }

    public synchronized void approveKyc(String phone) {
        Customer customer = userRepo.findCustomerByPhone(phone)
                .orElseThrow(() -> new ValidationException("Customer not found for KYC approval."));

        customer.setKycStatus(KycStatus.APPROVED);
        customer.setRejectionReason("");

        if (customer.getAccount() == null) {
            String accNum = String.valueOf(accountCounter.incrementAndGet());
            String cardNum = String.valueOf(cardCounter.incrementAndGet());
            CreditCard card = new CreditCard(cardNum);
            Account account = new Account(accNum, customer.getPhoneNumber(), card);

            customer.setAccount(account);
            accountRepo.save(account);
        }
    }

    public synchronized void rejectKyc(String phone, String reason) {
        Customer customer = userRepo.findCustomerByPhone(phone)
                .orElseThrow(() -> new ValidationException("Customer not found for KYC rejection."));

        if (reason == null || reason.trim().isEmpty()) {
            throw new ValidationException("Rejection reason cannot be empty.");
        }
        customer.setKycStatus(KycStatus.REJECTED);
        customer.setRejectionReason(reason.trim());
    }

    public synchronized void updateCustomerKycData(String phone, String first,
                                                   String last, String nationalId) {
        Customer customer = userRepo.findCustomerByPhone(phone)
                .orElseThrow(() -> new ValidationException("Customer not found."));

        customer.setFirstName(first);
        customer.setLastName(last);
        customer.setNationalCode(nationalId);
        customer.setKycStatus(KycStatus.PENDING);
        customer.setRejectionReason("");
    }

    public List<Customer> getPendingKycRequests() {
        return userRepo.findCustomersByKyc(KycStatus.PENDING);
    }
}