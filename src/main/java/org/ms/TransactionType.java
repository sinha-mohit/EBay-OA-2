package org.ms;

/**
 * Types of transactions supported by the simple banking system.
 */
public enum TransactionType {
    /** Money added to the account. */
    DEPOSIT,

    /** Money moved out of the account to another account. */
    TRANSFER_OUT,

    /** Money received from another account. */
    TRANSFER_IN
}
