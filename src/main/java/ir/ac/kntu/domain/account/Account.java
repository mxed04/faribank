package ir.ac.kntu.domain.account;

import ir.ac.kntu.exception.ValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Customer banking account managing balance, card, and transaction ledger.
 */
public class Account {
    private final String accountNumber;
    private final String ownerPhoneNumber;
    private double balance;
    private final CreditCard creditCard;
    private final List<Transaction> transactions;

    public Account(String accountNumber, String ownerPhoneNumber, CreditCard creditCard) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new ValidationException("Account number cannot be empty.");
        }
        if (ownerPhoneNumber == null || ownerPhoneNumber.isBlank()) {
            throw new ValidationException("Owner phone number cannot be empty.");
        }
        this.accountNumber = accountNumber.trim();
        this.ownerPhoneNumber = ownerPhoneNumber.trim();
        this.creditCard = creditCard;
        this.balance = 0.0;
        this.transactions = new ArrayList<>();
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerPhoneNumber() {
        return ownerPhoneNumber;
    }

    public double getBalance() {
        return balance;
    }

    public CreditCard getCreditCard() {
        return creditCard;
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public void charge(double amount, Transaction chargeTx) {
        if (amount <= 0) {
            throw new ValidationException("Charge amount must be strictly positive.");
        }
        this.balance += amount;
        if (chargeTx != null) {
            transactions.add(0, chargeTx);
        }
    }

    public void debit(double totalAmount, Transaction transferTx) {
        if (totalAmount <= 0) {
            throw new ValidationException("Debit amount must be strictly positive.");
        }
        if (this.balance < totalAmount) {
            throw new ValidationException("Insufficient funds.");
        }
        this.balance -= totalAmount;
        if (transferTx != null) {
            transactions.add(0, transferTx);
        }
    }

    public void credit(double amount, Transaction incomingTx) {
        if (amount <= 0) {
            throw new ValidationException("Credit amount must be strictly positive.");
        }
        this.balance += amount;
        if (incomingTx != null) {
            transactions.add(0, incomingTx);
        }
    }
}