package org.ms;

import java.util.*;

/**
 * A simple banking system implementation used for assessment/demo purposes.
 *
 * Responsibilities:
 * - Create accounts, deposits and transfers (Level 1)
 * - Provide ranking of top spenders (Level 2)
 * - Schedule and process payments with cashback (Level 3)
 * - Merge accounts and their histories (Level 4)
 *
 * The implementation favors clarity and basic data structures (HashMap, ArrayList,
 * PriorityQueue) and avoids advanced Java language features so it's easy to read.
 */
public class BankingSystem {
    /** Map of accountId -> Account */
    private Map<String, Account> accounts = new HashMap<>();

    /** Map of accountId -> list of scheduled payments originating from that account */
    private Map<String, List<ScheduledPayment>> scheduledPayments = new HashMap<>();

    // For demo/testing only: expose scheduled payments map (consider removing for production)
    public Map<String, List<ScheduledPayment>> getScheduledPayments() {
        return scheduledPayments;
    }

    /**
     * Create a new account if it does not already exist.
     *
     * @param accountId unique identifier for the account
     * @param timestamp creation time (integer timestamp used for the assignment)
     * @return true if account was created, false if it already existed
     */
    public boolean createAccount(String accountId, int timestamp) {
        if (accounts.containsKey(accountId)) return false;
        accounts.put(accountId, new Account(accountId, timestamp));
        return true;
    }

    /**
     * Deposit amount into an account.
     *
     * @param accountId account to deposit into
     * @param timestamp timestamp of deposit
     * @param amount positive amount to deposit
     * @return Optional containing new balance if successful, or empty Optional if account not found or invalid amount
     */
    public Optional<Integer> deposit(String accountId, int timestamp, int amount) {
        Account acc = accounts.get(accountId);
        if (acc == null || amount <= 0) return Optional.empty();
        acc.balance += amount; // update balance
        // record transaction (deposit)
        acc.addTransaction(new Transaction(accountId, timestamp, amount, TransactionType.DEPOSIT));
        return Optional.of(acc.balance);
    }

    /**
     * Transfer amount from one account to another.
     *
     * @param fromId source account id
     * @param toId destination account id
     * @param timestamp timestamp of transfer
     * @param amount positive amount to transfer
     * @return Optional containing remaining balance of source if successful, otherwise empty Optional
     */
    public Optional<Integer> transfer(String fromId, String toId, int timestamp, int amount) {
        Account from = accounts.get(fromId);
        Account to = accounts.get(toId);
        if (from == null || to == null || amount <= 0 || from.balance < amount) return Optional.empty();
        from.balance -= amount;
        to.balance += amount;
        // add transaction records for both accounts
        from.addTransaction(new Transaction(toId, timestamp, amount, TransactionType.TRANSFER_OUT));
        to.addTransaction(new Transaction(fromId, timestamp, amount, TransactionType.TRANSFER_IN));
        return Optional.of(from.balance);
    }

    /**
     * Return top N spenders (accounts with highest outgoing transfer amounts) up to a given timestamp.
     *
     * Sorting: primary by amount (descending), secondary by accountId (ascending).
     *
     * @param timestamp timestamp up to which outgoing amounts are considered
     * @param n number of top accounts to return
     * @return list of accountIds sorted by the ranking described
     */
    public List<String> topSpenders(int timestamp, int n) {
        // Use a priority queue with custom comparator to order by amount desc, id asc
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

    /**
     * Schedule a payment from one account to another at a future timestamp.
     *
     * @param accountId source account id
     * @param targetAccId destination account id
     * @param timestamp scheduled time for the payment
     * @param amount amount to transfer
     * @param cashbackPercentage percentage of cashback applied to sender when processed
     */
    public void schedulePayment(String accountId, String targetAccId, int timestamp, int amount, double cashbackPercentage) {
        ScheduledPayment payment = new ScheduledPayment(accountId, targetAccId, timestamp, amount, cashbackPercentage);
        // ensure list exists for this account, then add payment
        if (!scheduledPayments.containsKey(accountId)) {
            scheduledPayments.put(accountId, new ArrayList<ScheduledPayment>());
        }
        scheduledPayments.get(accountId).add(payment);
    }

    /**
     * Retrieve the PaymentStatus enum for a scheduled payment.
     *
     * Returns null when the payment is not found.
     *
     * @param accountId source account id where payment was scheduled
     * @param timestamp scheduled timestamp (used to match the payment)
     * @param paymentId unique payment identifier
     * @return PaymentStatus enum or null if not found
     */
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

    /**
     * Process all scheduled payments up to the provided timestamp.
     *
     * For each payment whose scheduled timestamp is <= currentTimestamp and whose
     * status is SCHEDULED, attempt to transfer funds. On success, apply cashback
     * to sender and mark the payment PROCESSED. On failure (e.g., insufficient funds)
     * mark it FAILED.
     *
     * @param currentTimestamp current time used to decide which scheduled payments to process
     */
    public void processScheduledPayments(int currentTimestamp) {
        for (List<ScheduledPayment> payments : scheduledPayments.values()) {
            for (ScheduledPayment p : payments) {
                // skip payments scheduled for the future or already processed/failed
                if (p.timestamp > currentTimestamp || p.status != PaymentStatus.SCHEDULED) continue;
                Account from = accounts.get(p.fromId);
                Account to = accounts.get(p.toId);
                if (from != null && to != null && from.balance >= p.amount) {
                    // perform the transfer
                    from.balance -= p.amount;
                    to.balance += p.amount;
                    // apply cashback to the sender (round down)
                    int cashback = (int) (p.amount * p.cashbackPercentage / 100.0);
                    from.balance += cashback;
                    p.status = PaymentStatus.PROCESSED;
                } else {
                    // insufficient funds or missing accounts
                    p.status = PaymentStatus.FAILED;
                }
            }
        }
    }

    /**
     * Merge `accountId2` into `accountId1`.
     *
     * Balances are combined and transaction histories appended. Scheduled payments that
     * originated from the merged account are moved to the surviving account.
     *
     * @param accountId1 target account that will remain after merge
     * @param accountId2 account that will be removed and merged into `accountId1`
     */
    public void mergeAccounts(String accountId1, String accountId2) {
        Account a1 = accounts.get(accountId1);
        Account a2 = accounts.get(accountId2);
        if (a1 == null || a2 == null) return;
        // combine balances and transaction histories
        a1.balance += a2.balance;
        a1.transactions.addAll(a2.transactions);
        // remove the merged account from the registry
        accounts.remove(accountId2);
        // Move scheduled payments from the removed account to the surviving account
        if (scheduledPayments.containsKey(accountId2)) {
            if (!scheduledPayments.containsKey(accountId1)) {
                scheduledPayments.put(accountId1, new ArrayList<ScheduledPayment>());
            }
            scheduledPayments.get(accountId1).addAll(scheduledPayments.get(accountId2));
            scheduledPayments.remove(accountId2);
        }
    }

}