package com.shane;

import java.sql.Timestamp;

public class Transaction {

    private int accountNumber;
    private char transactionType;
    private double transactionAmount;
    private int transferAccount;
    private Timestamp timestamp;

    public Transaction(int accountNumber) {
        this.accountNumber = accountNumber;
    }

    public int getAccountNumber() {
        return this.accountNumber;
    }

    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }
    public char getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(char transactionType) {
        this.transactionType = transactionType;
    }

    public double getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public int getTransferAccount() {
        return transferAccount;
    }

    public void setTransferAccount(int transferAccount) {
        this.transferAccount = transferAccount;
    }

    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

}
