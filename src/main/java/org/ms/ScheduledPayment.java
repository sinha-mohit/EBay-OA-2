package org.ms;

import java.util.UUID;

/**
 * Represents a scheduled payment between two accounts.
 *
 * Includes the source account (`fromId`), destination account (`toId`), scheduled
 * timestamp, amount to transfer, cashback percentage to apply to the sender,
 * and the current status of the scheduled payment.
 */
public class ScheduledPayment {
    /** Unique identifier for this scheduled payment. */
    String paymentId;

    /** Account that will send the funds. */
    String fromId;

    /** Account that will receive the funds. */
    String toId;

    /** Scheduled timestamp when this payment should be processed. */
    int timestamp;

    /** Amount to transfer when the payment is processed. */
    int amount;

    /** Percentage cashback applied to the sender when payment succeeds. */
    double cashbackPercentage;

    /** Current status of the scheduled payment (enum). */
    PaymentStatus status; // SCHEDULED, PROCESSED, FAILED

    /**
     * Create a new scheduled payment.
     *
     * @param from source account id
     * @param to destination account id
     * @param ts scheduled timestamp
     * @param amt amount to transfer
     * @param cashback cashback percentage (e.g., 10.0 for 10%)
     */
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
