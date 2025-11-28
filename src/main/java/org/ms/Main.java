package org.ms;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        BankingSystem bank = new BankingSystem();
        System.out.println("Create Account A: " + bank.createAccount("A", 1));
        System.out.println("Create Account B: " + bank.createAccount("B", 2));
        System.out.println("Deposit 1000 to A: " + bank.deposit("A", 3, 1000));
        System.out.println("Deposit 500 to B: " + bank.deposit("B", 4, 500));
        System.out.println("Transfer 300 from A to B: " + bank.transfer("A", "B", 5, 300));
        System.out.println("Top Spenders: " + bank.topSpenders(10, 2));
        bank.schedulePayment("A", "B", 6, 200, 10.0);
        String paymentId = bank.getScheduledPayments().get("A").get(0).paymentId;
        PaymentStatus statusBefore = bank.getPaymentStatus("A", 6, paymentId);
        System.out.println("Payment Status before processing: " + (statusBefore == null ? "not found" : statusBefore.name()));
        bank.processScheduledPayments(6);
        PaymentStatus statusAfter = bank.getPaymentStatus("A", 6, paymentId);
        System.out.println("Payment Status after processing: " + (statusAfter == null ? "not found" : statusAfter.name()));
        System.out.println("Merge B into A");
        bank.mergeAccounts("A", "B");
        System.out.println("Deposit 100 to merged A: " + bank.deposit("A", 7, 100));
        System.out.println("Top Spenders after merge: " + bank.topSpenders(10, 2));
    }
}
