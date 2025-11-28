package org.ms.banking;

import java.util.UUID;

public class ScheduledPayment {
    private final String id;
    private String fromAccountId;
    private String toAccountId;
    private final int scheduledAt;
    private final long amount;
    private final double cashbackPercentage;
    private PaymentStatus status;

    public ScheduledPayment(String fromAccountId, String toAccountId, int scheduledAt, long amount, double cashbackPercentage) {
        this.id = UUID.randomUUID().toString();
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.scheduledAt = scheduledAt;
        this.amount = amount;
        this.cashbackPercentage = cashbackPercentage;
        this.status = PaymentStatus.SCHEDULED;
    }

    public String getId() {
        return id;
    }

    public String getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(String fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public String getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(String toAccountId) {
        this.toAccountId = toAccountId;
    }

    public int getScheduledAt() {
        return scheduledAt;
    }

    public long getAmount() {
        return amount;
    }

    public double getCashbackPercentage() {
        return cashbackPercentage;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
}
