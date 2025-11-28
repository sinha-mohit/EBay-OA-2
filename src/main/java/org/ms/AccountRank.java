package org.ms;

/**
 * Helper class used when ranking accounts by outgoing spend.
 *
 * Contains the account id and the computed amount used for ordering.
 */
public class AccountRank {
    /** Account identifier used for tie-breaking and identification. */
    String accountId;

    /** Amount used for ranking (e.g., outgoing spend). */
    int amount;

    public AccountRank(String id, int amt) {
        this.accountId = id;
        this.amount = amt;
    }
}
