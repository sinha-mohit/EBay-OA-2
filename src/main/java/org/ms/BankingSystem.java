package org.ms;

import java.util.*;

public class BankingSystem {
    private Map<String, Account> accounts = new HashMap<>();
    private Map<String, List<ScheduledPayment>> scheduledPayments = new HashMap<>();

    // For demo/testing only
    public Map<String, List<ScheduledPayment>> getScheduledPayments() {
        return scheduledPayments;
    }

    public boolean createAccount(String accountId, int timestamp) {
        if (accounts.containsKey(accountId)) return false;
        accounts.put(accountId, new Account(accountId, timestamp));
        return true;
    }

    public Optional<Integer> deposit(String accountId, int timestamp, int amount) {
        Account acc = accounts.get(accountId);
        if (acc == null || amount <= 0) return Optional.empty();
        acc.balance += amount;
        acc.addTransaction(new Transaction(accountId, timestamp, amount, TransactionType.DEPOSIT));
        return Optional.of(acc.balance);
    }

    public Optional<Integer> transfer(String fromId, String toId, int timestamp, int amount) {
        Account from = accounts.get(fromId);
        Account to = accounts.get(toId);
        if (from == null || to == null || amount <= 0 || from.balance < amount) return Optional.empty();
        from.balance -= amount;
        to.balance += amount;
        from.addTransaction(new Transaction(toId, timestamp, amount, TransactionType.TRANSFER_OUT));
        to.addTransaction(new Transaction(fromId, timestamp, amount, TransactionType.TRANSFER_IN));
        return Optional.of(from.balance);
    }

    public List<String> topSpenders(int timestamp, int n) {
        PriorityQueue<AccountRank> pq = new PriorityQueue<>(new Comparator<AccountRank>() {
            public int compare(AccountRank a, AccountRank b) {
                if (b.amount != a.amount) return b.amount - a.amount;
                return a.accountId.compareTo(b.accountId);
            }
        });
        for (Account acc : accounts.values()) {
            int spent = acc.getOutgoingAmountUpTo(timestamp);
            pq.offer(new AccountRank(acc.accountId, spent));
        }
        List<String> result = new ArrayList<>();
        while (n-- > 0 && !pq.isEmpty()) {
            result.add(pq.poll().accountId);
        }
        return result;
    }

    public void schedulePayment(String accountId, String targetAccId, int timestamp, int amount, double cashbackPercentage) {
        ScheduledPayment payment = new ScheduledPayment(accountId, targetAccId, timestamp, amount, cashbackPercentage);
        if (!scheduledPayments.containsKey(accountId)) {
            scheduledPayments.put(accountId, new ArrayList<ScheduledPayment>());
        }
        scheduledPayments.get(accountId).add(payment);
    }

    public PaymentStatus getPaymentStatus(String accountId, int timestamp, String paymentId) {
        List<ScheduledPayment> payments = scheduledPayments.get(accountId);
        if (payments == null) return null;
        for (ScheduledPayment p : payments) {
            if (p.paymentId.equals(paymentId) && p.timestamp == timestamp) {
                return p.status;
            }
        }
        return null;
    }

    public void processScheduledPayments(int currentTimestamp) {
        for (List<ScheduledPayment> payments : scheduledPayments.values()) {
            for (ScheduledPayment p : payments) {
                if (p.timestamp > currentTimestamp || p.status != PaymentStatus.SCHEDULED) continue;
                Account from = accounts.get(p.fromId);
                Account to = accounts.get(p.toId);
                if (from != null && to != null && from.balance >= p.amount) {
                    from.balance -= p.amount;
                    to.balance += p.amount;
                    int cashback = (int) (p.amount * p.cashbackPercentage / 100.0);
                    from.balance += cashback;
                    p.status = PaymentStatus.PROCESSED;
                } else {
                    p.status = PaymentStatus.FAILED;
                }
            }
        }
    }

    public void mergeAccounts(String accountId1, String accountId2) {
        Account a1 = accounts.get(accountId1);
        Account a2 = accounts.get(accountId2);
        if (a1 == null || a2 == null) return;
        a1.balance += a2.balance;
        a1.transactions.addAll(a2.transactions);
        accounts.remove(accountId2);
        // Update scheduled payments to point to merged account
        if (scheduledPayments.containsKey(accountId2)) {
            if (!scheduledPayments.containsKey(accountId1)) {
                scheduledPayments.put(accountId1, new ArrayList<ScheduledPayment>());
            }
            scheduledPayments.get(accountId1).addAll(scheduledPayments.get(accountId2));
            scheduledPayments.remove(accountId2);
        }
    }

    // ...existing code...
}