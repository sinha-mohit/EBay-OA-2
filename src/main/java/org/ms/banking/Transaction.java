package org.ms.banking;

public class Transaction {
    private final int timestamp;
    private final long amount;

    public Transaction(int timestamp, long amount) {
        this.timestamp = timestamp;
        this.amount = amount;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public long getAmount() {
        return amount;
    }
}
