package org.ms.banking;

import java.util.*;

public class Account {
    private final String id;
    private long balance;
    // store raw outgoing events so we can rebuild prefix sums when merging
    private final List<Transaction> outgoingEvents = new ArrayList<>();
    // prefix sums keyed by timestamp -> cumulative outgoing amount up to that timestamp
    private final TreeMap<Integer, Long> outgoingPrefix = new TreeMap<>();

    public Account(String id) {
        this.id = id;
        this.balance = 0L;
    }

    public String getId() {
        return id;
    }

    public synchronized long getBalance() {
        return balance;
    }

    public synchronized void deposit(long amount) {
        if (amount <= 0) return;
        balance += amount;
    }

    public synchronized boolean withdraw(long amount) {
        if (amount <= 0) return false;
        if (balance < amount) return false;
        balance -= amount;
        return true;
    }

    public synchronized void addOutgoingEvent(int timestamp, long amount) {
        if (amount <= 0) return;
        outgoingEvents.add(new Transaction(timestamp, amount));
        rebuildPrefix();
    }

    private synchronized void rebuildPrefix() {
        outgoingPrefix.clear();
        outgoingEvents.sort(Comparator.comparingInt(Transaction::getTimestamp));
        long cum = 0L;
        for (Transaction t : outgoingEvents) {
            cum += t.getAmount();
            // if there are multiple events at same timestamp we keep the latest cumulative
            outgoingPrefix.put(t.getTimestamp(), cum);
        }
    }

    public synchronized long getOutgoingSumUpTo(int timestamp) {
        Map.Entry<Integer, Long> e = outgoingPrefix.floorEntry(timestamp);
        return e == null ? 0L : e.getValue();
    }

    public synchronized void mergeFrom(Account other) {
        if (other == null) return;
        this.balance += other.balance;
        // move outgoing events
        this.outgoingEvents.addAll(other.outgoingEvents);
        rebuildPrefix();
    }

    public synchronized List<Transaction> getOutgoingEvents() {
        return new ArrayList<>(outgoingEvents);
    }
}
