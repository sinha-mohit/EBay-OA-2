package org.ms;

import org.ms.banking.BankingSystem;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        BankingSystem bank = new BankingSystem();

        // create accounts
        bank.createAccount("A001", 1);
        bank.createAccount("B001", 1);
        bank.createAccount("C001", 1);

        // deposits
        bank.deposit("A001", 2, 1000);
        bank.deposit("B001", 2, 500);

        // transfers
        bank.transfer("A001", "B001", 3, 200);
        bank.transfer("A001", "C001", 4, 150);
        bank.transfer("B001", "C001", 5, 100);

        // top spenders up to timestamp 5 (A001:350, B001:100, C001:0)
        List<String> top2 = bank.topSpenders(5, 2);
        System.out.println("Top spenders up to t=5: " + top2);

        // schedule payment from A001 to B001 at t=10 with 5% cashback
        String pid = bank.schedulePayment("A001", "B001", 10, 300, 5.0);
        System.out.println("Scheduled payment id: " + pid);

        // check status before processing
        System.out.println("Payment status before processing: " + bank.getPaymentStatus("A001", 9, pid));

        // process scheduled payments at t=10
        bank.processScheduledPayments(10);
        System.out.println("Payment status after processing: " + bank.getPaymentStatus("A001", 10, pid));

        // balances
        System.out.println("Balance A001: " + bank.getAccount("A001").getBalance());
        System.out.println("Balance B001: " + bank.getAccount("B001").getBalance());

        // merge B001 into A001
        bank.mergeAccounts("A001", "B001");
        System.out.println("After merge, A001 balance: " + bank.getAccount("A001").getBalance());

        // top spenders after merge
        List<String> topAfterMerge = bank.topSpenders(20, 3);
        System.out.println("Top spenders after merge: " + topAfterMerge);
    }
}