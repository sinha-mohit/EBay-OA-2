package org.ms;

/**
 * Status values for scheduled payments.
 */
public enum PaymentStatus {
    /** Payment is scheduled but not yet processed. */
    SCHEDULED,

    /** Payment was processed successfully. */
    PROCESSED,

    /** Payment failed (insufficient funds or other error). */
    FAILED
}

