package ir.ac.kntu.repository;

import ir.ac.kntu.domain.user.Customer;
import ir.ac.kntu.domain.user.KycStatus;
import ir.ac.kntu.domain.user.SupportUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory repository managing customer and support credentials.
 */
public class UserRepository {
    private final Map<String, Customer> customersByPhone = new ConcurrentHashMap<>();
    private final Map<String, Customer> customersByNid = new ConcurrentHashMap<>();
    private final Map<String, SupportUser> supportByUsername = new ConcurrentHashMap<>();

    public UserRepository() {
        saveSupport(new SupportUser("Admin", "Support", "admin", "Admin@1234"));
    }

    public synchronized void saveCustomer(Customer customer) {
        customersByPhone.put(customer.getPhoneNumber(), customer);
        customersByNid.put(customer.getNationalCode(), customer);
    }

    public synchronized void saveSupport(SupportUser user) {
        supportByUsername.put(user.getUsername(), user);
    }

    public Optional<Customer> findCustomerByPhone(String phone) {
        if (phone == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(customersByPhone.get(phone.trim()));
    }

    public Optional<Customer> findCustomerByNationalCode(String code) {
        if (code == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(customersByNid.get(code.trim()));
    }

    public Optional<SupportUser> findSupportByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(supportByUsername.get(username.trim()));
    }

    public List<Customer> findCustomersByKyc(KycStatus status) {
        List<Customer> result = new ArrayList<>();
        for (Customer cust : customersByPhone.values()) {
            if (cust.getKycStatus() == status) {
                result.add(cust);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public List<Customer> getAllCustomers() {
        return List.copyOf(customersByPhone.values());
    }
}