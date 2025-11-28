package org.ms;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bank account.
 *
 * Stores basic account information and a list of transactions associated with the account.
 */
public class Account {
    /** Unique account identifier. */
    String accountId;

    /** Timestamp when the account was created (optional, retained for audit/history). */
    int createdAt;

    /** Current account balance (in smallest currency unit, e.g., cents). */
    int balance;

    /** List of transactions for this account (deposits, transfers in/out). */
    List<Transaction> transactions = new ArrayList<>();

    /**
     * Create a new Account instance.
     *
     * @param id account identifier
     * @param ts creation timestamp
     */
    public Account(String id, int ts) {
        this.accountId = id;
        this.createdAt = ts;
        this.balance = 0;
    }

    /**
     * Add a transaction record to this account's history.
     *
     * @param t transaction to add
     */
    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    /**
     * Calculate the total outgoing (transfer out) amount up to the given timestamp.
     *
     * This is used by ranking logic (top spenders) to compute how much an account
     * has spent up to a certain time.
     *
     * @param timestamp inclusive upper bound for transaction timestamps
     * @return sum of outgoing amounts up to provided timestamp
     */
    public int getOutgoingAmountUpTo(int timestamp) {
        int sum = 0;
        for (Transaction t : transactions) {
            // Only count outgoing transfers and only those before or at timestamp
            if (t.type == TransactionType.TRANSFER_OUT && t.timestamp <= timestamp) {
                sum += t.amount;
            }
        }
        return sum;
    }
}
