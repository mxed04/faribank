package ir.ac.kntu.domain.account;

import java.time.Instant;
import java.util.Objects;

/**
 * Transfer receipt encapsulation presenting transaction confirmation data.
 */
public class TransferReceipt {
    private final Transaction transaction;

    public TransferReceipt(Transaction transaction) {
        this.transaction = Objects.requireNonNull(transaction, "Transaction cannot be null.");
    }

    public String getTrackingNumber() {
        return transaction.getTrackingNumber();
    }

    public String getSourceAccount() {
        return transaction.getSourceAccount();
    }

    public String getDestAccount() {
        return transaction.getDestAccount();
    }

    public String getDestOwnerName() {
        return transaction.getDestOwnerName();
    }

    public double getAmount() {
        return transaction.getAmount();
    }

    public double getFee() {
        return transaction.getFee();
    }

    public double getTotalDeduction() {
        return transaction.getTotalDeduction();
    }

    public Instant getTimestamp() {
        return transaction.getTimestamp();
    }
}