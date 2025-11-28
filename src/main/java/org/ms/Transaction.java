package org.ms;

/**
 * Simple transaction record used by `Account`.
 *
 * The record stores the other account involved (for transfers), the timestamp,
 * the amount and the transaction type (deposit, transfer in, transfer out).
 */
public class Transaction {
    /** The other account involved in this transaction (for transfers). */
    String otherAccountId;

    /** Timestamp when the transaction occurred. */
    int timestamp;

    /** Transaction amount (positive integer). */
    int amount;

    /** Type of transaction (enum). */
    TransactionType type;

    /**
     * Construct a Transaction record.
     *
     * @param otherId other account associated with this transaction (for transfers)
     * @param ts timestamp of the transaction
     * @param amt amount for the transaction
     * @param type transaction type enum
     */
    public Transaction(String otherId, int ts, int amt, TransactionType type) {
        this.otherAccountId = otherId;
        this.timestamp = ts;
        this.amount = amt;
        this.type = type;
    }
}
