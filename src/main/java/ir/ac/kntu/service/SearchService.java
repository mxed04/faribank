package ir.ac.kntu.service;

import ir.ac.kntu.domain.contact.Contact;
import ir.ac.kntu.domain.user.Customer;
import ir.ac.kntu.repository.ContactRepository;
import ir.ac.kntu.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Service providing fuzzy string matching and Levenshtein similarity search.
 */
public class SearchService {
    private final UserRepository userRepo;
    private final ContactRepository contactRepo;

    public SearchService(UserRepository userRepo, ContactRepository contactRepo) {
        this.userRepo = Objects.requireNonNull(userRepo, "User repo cannot be null.");
        this.contactRepo = Objects.requireNonNull(contactRepo, "Contact repo cannot be null.");
    }

    public List<Contact> searchContactsFuzzy(String userPhone, String query, double threshold) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        List<Contact> allContacts = contactRepo.getContacts(userPhone);
        List<Contact> matches = new ArrayList<>();

        for (Contact contact : allContacts) {
            double firstScore = computeScore(contact.getFirstName(), query);
            double lastScore = computeScore(contact.getLastName(), query);
            double fullScore = computeScore(contact.getFullName(), query);
            double phoneScore = computeScore(contact.getPhoneNumber(), query);
            double bestScore = Math.max(Math.max(firstScore, lastScore), Math.max(fullScore, phoneScore));

            if (bestScore >= threshold) {
                matches.add(contact);
            }
        }
        return Collections.unmodifiableList(matches);
    }

    public List<Customer> searchCustomersFuzzy(String query, double threshold) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        List<Customer> allCustomers = userRepo.getAllCustomers();
        List<Customer> matches = new ArrayList<>();

        for (Customer customer : allCustomers) {
            double firstScore = computeScore(customer.getFirstName(), query);
            double lastScore = computeScore(customer.getLastName(), query);
            double fullScore = computeScore(customer.getFullName(), query);
            double phoneScore = computeScore(customer.getPhoneNumber(), query);
            double bestScore = Math.max(Math.max(firstScore, lastScore), Math.max(fullScore, phoneScore));

            if (bestScore >= threshold) {
                matches.add(customer);
            }
        }
        return Collections.unmodifiableList(matches);
    }

    public double computeScore(String targetText, String queryText) {
        if (targetText == null || queryText == null) {
            return 0.0;
        }

        String normTarget = targetText.trim().toLowerCase();
        String normQuery = queryText.trim().toLowerCase();

        if (normTarget.equals(normQuery)) {
            return 1.0;
        }

        if (normTarget.contains(normQuery)) {
            return 0.85;
        }

        int maxLen = Math.max(normTarget.length(), normQuery.length());

        int distance = calculateLevenshtein(normTarget, normQuery);
        return 1.0 - ((double) distance / (double) maxLen);
    }

    public int calculateLevenshtein(String firstText, String secondText) {
        if (firstText == null || secondText == null) {
            return Integer.MAX_VALUE;
        }

        int lenFirst = firstText.length();
        int lenSecond = secondText.length();

        if (lenFirst == 0) {
            return lenSecond;
        }
        if (lenSecond == 0) {
            return lenFirst;
        }

        int[][] distMatrix = new int[lenFirst + 1][lenSecond + 1];

        for (int rowIdx = 0; rowIdx <= lenFirst; rowIdx++) {
            distMatrix[rowIdx][0] = rowIdx;
        }
        for (int colIdx = 0; colIdx <= lenSecond; colIdx++) {
            distMatrix[0][colIdx] = colIdx;
        }

        for (int rowIdx = 1; rowIdx <= lenFirst; rowIdx++) {
            for (int colIdx = 1; colIdx <= lenSecond; colIdx++) {
                int cost = firstText.charAt(rowIdx - 1) == secondText.charAt(colIdx - 1) ? 0 : 1;
                int deletion = distMatrix[rowIdx - 1][colIdx] + 1;
                int insertion = distMatrix[rowIdx][colIdx - 1] + 1;
                int substitution = distMatrix[rowIdx - 1][colIdx - 1] + cost;

                distMatrix[rowIdx][colIdx] = Math.min(Math.min(deletion, insertion), substitution);
            }
        }

        return distMatrix[lenFirst][lenSecond];
    }
}