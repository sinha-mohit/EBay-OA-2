package org.ms;

public class Transaction {
    String otherAccountId;
    int timestamp;
    int amount;
    TransactionType type;

    public Transaction(String otherId, int ts, int amt, TransactionType type) {
        this.otherAccountId = otherId;
        this.timestamp = ts;
        this.amount = amt;
        this.type = type;
    }
}
