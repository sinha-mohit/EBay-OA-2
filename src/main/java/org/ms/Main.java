package org.ms;

/**
 * Small demo runner for the BankingSystem.
 *
 * Exercises account creation, deposits, transfers, scheduled payments and merging.
 */
public class Main {
    public static void main(String[] args) {
        BankingSystem bank = new BankingSystem();

        // Create two accounts
        System.out.println("Create Account A: " + bank.createAccount("A", 1));
        System.out.println("Create Account B: " + bank.createAccount("B", 2));

        // Deposits
        System.out.println("Deposit 1000 to A: " + bank.deposit("A", 3, 1000));
        System.out.println("Deposit 500 to B: " + bank.deposit("B", 4, 500));

        // Transfer from A to B
        System.out.println("Transfer 300 from A to B: " + bank.transfer("A", "B", 5, 300));

        // Show top spenders up to timestamp 10
        System.out.println("Top Spenders: " + bank.topSpenders(10, 2));

        // Schedule a payment from A to B at timestamp 6 with 10% cashback
        bank.schedulePayment("A", "B", 6, 200, 10.0);
        // Retrieve the generated payment id for demo purposes (not part of public API normally)
        String paymentId = bank.getScheduledPayments().get("A").get(0).paymentId;

        // Query status (may return null if not found)
        PaymentStatus statusBefore = bank.getPaymentStatus("A", 6, paymentId);
        System.out.println("Payment Status before processing: " + (statusBefore == null ? "not found" : statusBefore.name()));

        // Process scheduled payments at timestamp 6
        bank.processScheduledPayments(6);

        // Query status again
        PaymentStatus statusAfter = bank.getPaymentStatus("A", 6, paymentId);
        System.out.println("Payment Status after processing: " + (statusAfter == null ? "not found" : statusAfter.name()));

        // Demonstrate merging accounts
        System.out.println("Merge B into A");
        bank.mergeAccounts("A", "B");

        // Further deposit after merge
        System.out.println("Deposit 100 to merged A: " + bank.deposit("A", 7, 100));
        System.out.println("Top Spenders after merge: " + bank.topSpenders(10, 2));
    }
}
