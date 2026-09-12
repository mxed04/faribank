package ir.ac.kntu.service;

import ir.ac.kntu.domain.contact.Contact;
import ir.ac.kntu.domain.user.Customer;
import ir.ac.kntu.repository.AccountRepository;
import ir.ac.kntu.repository.ContactRepository;
import ir.ac.kntu.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchServiceTest {
    private SearchService searchService;
    private Customer owner;

    @BeforeEach
    void setUp() {
        UserRepository userRepo = new UserRepository();
        AccountRepository accountRepo = new AccountRepository();
        ContactRepository contactRepo = new ContactRepository();
        AuthService authService = new AuthService(userRepo, accountRepo);
        searchService = new SearchService(userRepo, contactRepo);

        owner = new Customer("Ali", "Karimi", "09121112233", "0011223344", "Ali@1234");
        Customer other = new Customer("Mohammad", "Afra", "09123334455", "1122334455", "Pass@1234");

        authService.registerCustomer(owner);
        authService.registerCustomer(other);

        contactRepo.saveContact(owner.getPhoneNumber(), new Contact("Farhad", "Majidi", "09125556677"));
        contactRepo.saveContact(owner.getPhoneNumber(), new Contact("Sohrab", "Sepehri", "09127778899"));
    }

    @Test
    void testExactSimilarityScore() {
        double score = searchService.computeScore("Farhad", "Farhad");
        assertEquals(1.0, score, 0.001);
    }

    @Test
    void testLevenshteinDistanceCalculation() {
        int dist = searchService.calculateLevenshtein("kitten", "sitting");
        assertEquals(3, dist);

        int sameDist = searchService.calculateLevenshtein("test", "test");
        assertEquals(0, sameDist);
    }

    @Test
    void testFuzzySearchContactsWithTypo() {
        List<Contact> results = searchService.searchContactsFuzzy(owner.getPhoneNumber(), "Farhd", 0.70);
        assertEquals(1, results.size());
        assertEquals("Farhad Majidi", results.get(0).getFullName());
    }

    @Test
    void testFuzzySearchCustomersWithTypo() {
        List<Customer> results = searchService.searchCustomersFuzzy("Mohamad", 0.70);
        assertEquals(1, results.size());
        assertEquals("Mohammad", results.get(0).getFirstName());
    }

    @Test
    void testFuzzySearchRejectsBelowThreshold() {
        List<Customer> results = searchService.searchCustomersFuzzy("RandomNonSense", 0.70);
        assertTrue(results.isEmpty());
    }
}