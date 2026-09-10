package ir.ac.kntu.domain.account;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents immutable financial ledger transactions with timestamp from Calendar.
 */
public class Transaction implements Comparable<Transaction> {
    private final String trackingNumber;
    private final TransactionType type;
    private final double amount;
    private final double fee;
    private final String sourceAccount;
    private final String destAccount;
    private final String destOwnerName;
    private final Instant timestamp;

    public Transaction(String trackingNumber, TransactionType type, double amount,
                       double fee, String sourceAccount, String destAccount,
                       String destOwnerName, Instant timestamp) {
        this.trackingNumber = Objects.requireNonNull(trackingNumber, "Tracking number is mandatory");
        this.type = Objects.requireNonNull(type, "Transaction type is mandatory");
        this.amount = amount;
        this.fee = fee;
        this.sourceAccount = sourceAccount;
        this.destAccount = destAccount;
        this.destOwnerName = destOwnerName;
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp is mandatory");
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public TransactionType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public double getFee() {
        return fee;
    }

    public double getTotalDeduction() {
        return amount + fee;
    }

    public String getSourceAccount() {
        return sourceAccount;
    }

    public String getDestAccount() {
        return destAccount;
    }

    public String getDestOwnerName() {
        return destOwnerName;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public int compareTo(Transaction other) {
        return other.timestamp.compareTo(this.timestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Transaction other = (Transaction) obj;
        return Objects.equals(trackingNumber, other.trackingNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trackingNumber);
    }
}