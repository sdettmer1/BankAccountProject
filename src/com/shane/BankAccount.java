package com.shane;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class BankAccount {
    private int accountNumber;
    private String customerName;
    private int PIN;
    private double balance;
    private String status = "";
    private List<Transaction> transactions;

//    public BankAccount() {
////       this.PIN = PIN;
////        this.accountNumber = "0123456789";
////        this.customerName = "Joe Smith";
////        this.balance = 1000.00;
////        this.status = "Welcome, " + this.customerName;
//
//    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public int getPIN() {
        return PIN;
    }

    public void setPIN(int customerID) {
        this.PIN = customerID;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Transaction> getTransactions() {

        DataSource dataSource = DataSource.getDataSourceInstance();
        transactions = new ArrayList<>();
        transactions = dataSource.getTransactions(this.getAccountNumber());

        return transactions;
    }

    public BankAccount getAccount() {

        DataSource dataSource = DataSource.getDataSourceInstance();
        if(this == null) {
            System.out.println("Null object");
        }
        return dataSource.getAccountInfo(this);
//        return bankAccount;
    }

    public boolean deposit(double amount) {


        this.balance += amount;
        Transaction transaction = new Transaction(this.accountNumber);
        transaction.setTransactionAmount(amount);
        transaction.setTransactionType('D');
        transaction.setTransferAccount(0);

        DataSource dataSource = DataSource.getDataSourceInstance();

        if(dataSource.updateForTransaction(this, transaction)) {
            this.status = "Deposit completed Successfully";
        } else {
            this.status = "Deposit failed.  Please try again later";
            this.balance -= amount;
            return false;
        }


        return true;
    }

    public boolean withdrawal(double amount) {

        if(amount > this.balance) {
            this.status = "There are not sufficient funds for this withdrawal";
            return false;
        }

        this.balance -= amount;
        Transaction transaction = new Transaction(this.accountNumber);
        transaction.setTransactionAmount(amount);
        transaction.setTransactionType('W');
        transaction.setTransferAccount(0);

        DataSource dataSource = DataSource.getDataSourceInstance();

        if(dataSource.updateForTransaction(this, transaction)) {
            this.status = "Withdrawal completed successfully.";

        } else {
            this.status = "Withdrawal failed. Please try again later";
            this.balance += amount;
            return false;
        }

        return true;

    }

    public boolean transferToAccount(double amount, BankAccount toBankAccount) {

        if(amount > this.balance) {
            this.status = "There are not sufficient funds to complete this transfer";
            return false;
        }

        this.balance -= amount;
        Transaction fromTransaction = new Transaction(this.getAccountNumber());
        fromTransaction.setTransactionAmount(amount);
        fromTransaction.setTransactionType('W');
        fromTransaction.setTransferAccount(toBankAccount.getAccountNumber());

        toBankAccount.setBalance(toBankAccount.getBalance() + amount);
        Transaction toTransaction = new Transaction(toBankAccount.getAccountNumber());
        toTransaction.setTransactionAmount(amount);
        toTransaction.setTransactionType('D');
        toTransaction.setTransferAccount(this.getAccountNumber());

        DataSource dataSource = DataSource.getDataSourceInstance();

        if(dataSource.transferFunds(this, fromTransaction, toBankAccount, toTransaction, amount)) {
            NumberFormat format = NumberFormat.getCurrencyInstance();
            this.status = "Transfer completed successfully.  Your new balance is " + format.format(this.getBalance());
        } else {
            this.balance += amount;
            this.status = "Transfer could not be completed successfully.";
            toBankAccount.setBalance(toBankAccount.getBalance() + amount);
        }

        return true;
    }


}
