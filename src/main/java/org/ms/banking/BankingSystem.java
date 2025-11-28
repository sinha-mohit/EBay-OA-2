package org.ms.banking;

import java.util.*;

public class BankingSystem {
    private final Map<String, Account> accounts = new HashMap<>();
    private final Map<String, ScheduledPayment> scheduledPayments = new HashMap<>();
    // maintained totals for fast top-spenders queries (all-time up to latestTimestamp)
    private final Map<String, Long> totalOutgoing = new HashMap<>();
    private final NavigableSet<AccountRank> ranking = new TreeSet<>(AccountRank.COMPARATOR);
    // latest timestamp we've processed (used to decide if fast ranking is valid for a requested timestamp)
    private int latestProcessedTimestamp = 0;

    // Level 1
    public synchronized boolean createAccount(String accountId, int timestamp) {
        if (accountId == null || accountId.isEmpty()) return false;
        if (accounts.containsKey(accountId)) return false;
        accounts.put(accountId, new Account(accountId));
        // initialize ranking structures
        totalOutgoing.put(accountId, 0L);
        ranking.add(new AccountRank(accountId, 0L));
        return true;
    }

    public boolean deposit(String accountId, int timestamp, int amount) {
        Account a = accounts.get(accountId);
        if (a == null) return false;
        a.deposit(amount);
        return true;
    }

    public boolean transfer(String fromId, String toId, int timestamp, int amount) {
        if (fromId == null || toId == null) return false;
        Account from = accounts.get(fromId);
        Account to = accounts.get(toId);
        if (from == null || to == null) return false;
        synchronized (this) {
            if (!from.withdraw(amount)) return false;
            to.deposit(amount);
            from.addOutgoingEvent(timestamp, amount);
            // update maintained totals and ranking for fast queries
            updateOutgoingTotal(fromId, amount);
            latestProcessedTimestamp = Math.max(latestProcessedTimestamp, timestamp);
        }
        return true;
    }

    // Level 2
    // Returns top N accountIds based on outgoing transactions up to timestamp
    public List<String> topSpenders(int timestamp, int n) {
        if (n <= 0) return Collections.emptyList();
        // If requested timestamp is at or beyond the latest processed timestamp, we can use the
        // maintained ranking which is much faster for frequent queries. Otherwise fall back to
        // computing using outgoing prefix sums per account.
        if (timestamp >= latestProcessedTimestamp) {
            return topSpendersFast(n);
        }
        // Build a simple list of account totals using a small helper class for clarity
        List<AccountTotal> list = new ArrayList<AccountTotal>();
        for (Map.Entry<String, Account> e : accounts.entrySet()) {
            String id = e.getKey();
            long out = e.getValue().getOutgoingSumUpTo(timestamp);
            list.add(new AccountTotal(id, out));
        }
        // sort by total desc, then id asc
        Collections.sort(list, new Comparator<AccountTotal>() {
            @Override
            public int compare(AccountTotal a, AccountTotal b) {
                int cmp = Long.compare(b.total, a.total);
                if (cmp != 0) return cmp;
                return a.accountId.compareTo(b.accountId);
            }
        });
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < list.size() && result.size() < n; i++) {
            result.add(list.get(i).accountId);
        }
        return result;
    }

    // Fast top spenders using the maintained ranking (O(n) to collect top n where n requested)
    private synchronized List<String> topSpendersFast(int n) {
        List<String> out = new ArrayList<>(Math.min(n, ranking.size()));
        int count = 0;
        for (AccountRank r : ranking) {
            if (count++ >= n) break;
            out.add(r.accountId);
        }
        return out;
    }

    // Update totalOutgoing and ranking when an outgoing amount increases for an account
    private synchronized void updateOutgoingTotal(String accountId, long delta) {
        long old = totalOutgoing.getOrDefault(accountId, 0L);
        // remove old rank
        ranking.remove(new AccountRank(accountId, old));
        long neu = old + delta;
        totalOutgoing.put(accountId, neu);
        ranking.add(new AccountRank(accountId, neu));
    }

    // Level 3
    // returns paymentId for reference
    public String schedulePayment(String accountId, String targetAccId, int timestamp, int amount, double cashbackPercentage) {
        if (!accounts.containsKey(accountId) || !accounts.containsKey(targetAccId)) return null;
        ScheduledPayment p = new ScheduledPayment(accountId, targetAccId, timestamp, amount, cashbackPercentage);
        scheduledPayments.put(p.getId(), p);
        latestProcessedTimestamp = Math.max(latestProcessedTimestamp, timestamp);
        return p.getId();
    }

    public String getPaymentStatus(String accountId, int timestamp, String paymentId) {
        ScheduledPayment p = scheduledPayments.get(paymentId);
        if (p == null) return "not_found";
        // basic auth: only owner of payment (from) can query
        if (!p.getFromAccountId().equals(accountId)) return "unauthorized";
        return p.getStatus().name().toLowerCase();
    }

    public void processScheduledPayments(int currentTimestamp) {
        List<ScheduledPayment> toProcess = new ArrayList<>();
        for (ScheduledPayment p : scheduledPayments.values()) {
            if (p.getStatus() == PaymentStatus.SCHEDULED && p.getScheduledAt() <= currentTimestamp) {
                toProcess.add(p);
            }
        }
        for (ScheduledPayment p : toProcess) {
            boolean ok = transfer(p.getFromAccountId(), p.getToAccountId(), p.getScheduledAt(), (int) p.getAmount());
            if (ok) {
                // apply cashback to payer
                int cashback = (int) Math.round(p.getAmount() * p.getCashbackPercentage() / 100.0);
                Account payer = accounts.get(p.getFromAccountId());
                if (payer != null && cashback > 0) payer.deposit(cashback);
                p.setStatus(PaymentStatus.PROCESSED);
            } else {
                p.setStatus(PaymentStatus.FAILED);
            }
        }
    }

    // Level 4
    public synchronized void mergeAccounts(String accountId1, String accountId2) {
        if (accountId1 == null || accountId2 == null) return;
        if (accountId1.equals(accountId2)) return;
        Account a1 = accounts.get(accountId1);
        Account a2 = accounts.get(accountId2);
        if (a1 == null || a2 == null) return;
        // merge balances and outgoing events
        a1.mergeFrom(a2);
        // update scheduled payments references
        for (ScheduledPayment p : scheduledPayments.values()) {
            if (p.getFromAccountId().equals(accountId2)) p.setFromAccountId(accountId1);
            if (p.getToAccountId().equals(accountId2)) p.setToAccountId(accountId1);
        }
        // remove account2
        accounts.remove(accountId2);
    }

    // helpers for demo/tests
    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    // Small helper to hold account id and its outgoing total for sorting clarity
    private static class AccountTotal {
        String accountId;
        long total;

        AccountTotal(String accountId, long total) {
            this.accountId = accountId;
            this.total = total;
        }
    }

    // Helper class used for ranking accounts by total outgoing amount (desc), then id (asc)
    private static class AccountRank {
        final String accountId;
        final long amount;

        static final Comparator<AccountRank> COMPARATOR = new Comparator<AccountRank>() {
            @Override
            public int compare(AccountRank a, AccountRank b) {
                int cmp = Long.compare(b.amount, a.amount); // desc
                if (cmp != 0) return cmp;
                return a.accountId.compareTo(b.accountId); // asc
            }
        };

        AccountRank(String accountId, long amount) {
            this.accountId = accountId;
            this.amount = amount;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AccountRank that = (AccountRank) o;
            return amount == that.amount && accountId.equals(that.accountId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, amount);
        }
    }
}
