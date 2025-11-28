package org.ms;

import java.util.UUID;

public class ScheduledPayment {
    String paymentId;
    String fromId;
    String toId;
    int timestamp;
    int amount;
    double cashbackPercentage;
    PaymentStatus status; // SCHEDULED, PROCESSED, FAILED

    public ScheduledPayment(String from, String to, int ts, int amt, double cashback) {
        this.paymentId = UUID.randomUUID().toString();
        this.fromId = from;
        this.toId = to;
        this.timestamp = ts;
        this.amount = amt;
        this.cashbackPercentage = cashback;
        this.status = PaymentStatus.SCHEDULED;
    }
}
