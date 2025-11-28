package org.ms;

import java.util.ArrayList;
import java.util.List;

public class Account {
    String accountId;
    int createdAt;
    int balance;
    List<Transaction> transactions = new ArrayList<>();

    public Account(String id, int ts) {
        this.accountId = id;
        this.createdAt = ts;
        this.balance = 0;
    }

    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    public int getOutgoingAmountUpTo(int timestamp) {
        int sum = 0;
        for (Transaction t : transactions) {
            if (t.type == TransactionType.TRANSFER_OUT && t.timestamp <= timestamp) {
                sum += t.amount;
            }
        }
        return sum;
    }
}
