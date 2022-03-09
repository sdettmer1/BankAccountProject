package com.shane;

import java.text.NumberFormat;
import java.util.List;
import java.util.Scanner;

public class Main {

    static Scanner scanner = new Scanner(System.in);
    static BankAccount bankAccount;
    static BankAccount toBankAccount;
    static DataSource dataSource;
    static Boolean loggedOn = false;
    static char choice = ' ';

    public static void main(String[] args) {

        dataSource = DataSource.getDataSourceInstance();
        dataSource.open();

        System.out.println("Please log on to your account using your PIN:");
        do {
            selectAccount();
            if(choice == '9') {
                break;
            }
        } while(!loggedOn);


        while(choice != '9') {
            choice = scanner.next().charAt(0);

            switch(choice) {
                case '1':
                    loggedOn = false;
                    selectAccount();
                    break;
                case '2':
                    checkBalance();
                    displayPrompt();
                    break;
                case '3':
                    makeDeposit();
                    displayPrompt();
                    break;
                case '4':
                    makeWithdrawal();
                    displayPrompt();
                    break;
                case '5':
                    listPreviousTransactions();
                    displayPrompt();
                    break;
                case '6':
                    transferFunds();
                    displayPrompt();
                    break;
                case '7':
                    displayMenu();
                    break;
                case '9':
                    System.out.println("Logging off now...  Goodbye");
                    dataSource.close();
                    break;
                default:
                    System.out.println("You have entered an invalid choice. Try again:");
                    displayMenu();
            }
        }
    }



    // OPTION 1: PROMPT THE USER TO ENTER PIN NUMBER TO "LOG ON" AND THEN FIND THE CORRECT ACCOUNT
    public static boolean selectAccount() {

        System.out.println("Enter PIN or 'END' to logoff:");
        String accountID = scanner.next();

        if(accountID.equalsIgnoreCase("END")) {
            System.out.println("Ending Session...");
            choice = '9';
            return false;
        }
        if(accountID.length() != 4) {
            System.out.println("Account ID is invalid...");
            return false;
        }

        try {
            int numericPIN = Integer.parseInt(accountID);
            bankAccount = new BankAccount();
            bankAccount.setPIN(numericPIN);
            bankAccount = dataSource.getAccountInfo(bankAccount);
            if(bankAccount == null) {
                System.out.println("Account with PIN " + accountID + " was not found");
                return false;

            } else {
                displayMenu();
                System.out.println("Welcome " + bankAccount.getCustomerName());
                System.out.println("What would you like to do today?");
            }
        } catch(NumberFormatException e) {
            System.out.println("Login failed: Account ID is not numeric");
        }

        loggedOn = true;
        return true;


    }

    // OPTION 2: RETRIEVE THE BALANCE FROM THE CURRENT BankAccount INSTANCE
    public static void checkBalance() {
        NumberFormat format = NumberFormat.getCurrencyInstance();
        System.out.println("Your current balance is " + format.format(bankAccount.getBalance()));
    }

    // OPTION 3: FIRST ENSURE THAT THE ENTERED AMOUNT IS NUMERIC THEN UPDATE THE BankAccount INSTANCE AND THE DATASOURCE
    public static void makeDeposit() {

        boolean validAmount = false;
        double amount = 0;
        System.out.println("Enter amount to deposit: ");


        do {

            if(scanner.hasNextDouble()) {
                validAmount = true;
                amount = scanner.nextDouble();
                bankAccount.deposit(amount);
                System.out.println(bankAccount.getStatus());
            } else {
                String junk = scanner.next();
                System.out.println(junk);
                System.out.println("Invalid Amount. Please enter amount to deposit again:");

            }
        } while (!validAmount);

    }

    // OPTION 4: FIRST ENSURE THAT THE ENTERED AMOUNT IS NUMERIC THEN UPDATE THE BankAccount INSTANCE AND THE DATASOURCE
    public static void makeWithdrawal() {

        boolean validAmount = false;
        double amount = 0;
        System.out.println("Enter amount to withdraw: ");



        do {

            if(scanner.hasNextDouble()) {
                validAmount = true;
                amount = scanner.nextDouble();
                bankAccount.withdrawal(amount);
                System.out.println(bankAccount.getStatus());
            } else {
                scanner.next();
                System.out.println("Invalid Amount. Please enter amount to withdraw again:");

            }

        } while (!validAmount);

    }

    // OPTION 5: USING THE ACCOUNT NUMBER OF THE CURRENT BankAccount INSTANCE, RETRIEVE ALL OF THE TRANSACTIONS
    // RELATED TO THAT ACCOUNT NUMBER
    public static void listPreviousTransactions() {

        List<Transaction> transactions = bankAccount.getTransactions();

        if (transactions == null || transactions.isEmpty()) {
            System.out.println("No Transactions found for this account.");
            return;
        }

        int transactionNumber = 0;

        NumberFormat format = NumberFormat.getCurrencyInstance();

        System.out.println("Trans   Type     Amount       Xfer Account           Date/Time");
        for(Transaction transaction : transactions) {
            transactionNumber++;
            System.out.println(transactionNumber + "         " + transaction.getTransactionType() + "         " +
                    (transaction.getTransactionType() == 'W' ?
                            format.format(-1 * transaction.getTransactionAmount()) :
                            format.format(transaction.getTransactionAmount()) ) + "  " +
                    (transaction.getTransferAccount() > 0 ? transaction.getTransferAccount() + "        " : "                " ) +
                     transaction.getTimestamp());
        }
    }

    // OPTION 6: FIRST ENSURE THAT THE ACCOUNT NUMBER ENTERED IS VALID
    // SECOND, ENSURE THAT THE ENTERED DOLLAR AMOUNT IS NUMERIC
    // THIRD, USE THE ENTERED ACCOUNT NUMBER AND THE ACCOUNT NUMBER OF THE CURRENT BankAccount INSTANCE TO TRANSFER THE FUNDS
    public static void transferFunds() {

        boolean validAccount = false;
        int transferAccount = 0;

        System.out.println("Enter the Bank Account Number of the Account to transfer funds to:");

        do {

            if(scanner.hasNextInt()) {
                validAccount = true;
                transferAccount = scanner.nextInt();
                toBankAccount = dataSource.getAccount(transferAccount);
                if(toBankAccount == null) {
                    validAccount = false;
                    System.out.println("Account number " + transferAccount + " was not found");
                    return;
                }
                System.out.println(bankAccount.getStatus());
            } else {
                scanner.next();
                System.out.println("Invalid Account Number. Please enter account number again:");

            }

        } while (!validAccount);

        System.out.println("Enter the Amount to transfer to Account #" + toBankAccount.getAccountNumber());

        double amount = 0;
        boolean validAmount = false;

        do {

            if(scanner.hasNextDouble()) {
                validAmount = true;
                amount = scanner.nextDouble();
                bankAccount.transferToAccount(amount, toBankAccount);
                System.out.println(bankAccount.getStatus());
            } else {
                scanner.next();
                System.out.println("Invalid Amount. Please enter amount to withdraw again:");

            }

        } while (!validAmount);

    }

    // OPTION 7: DISPLAY THE MENU OF CHOICES
    public static void displayMenu() {

        System.out.println("Select an Option:\n");
        System.out.println("1.  Select New Bank Account");
        System.out.println("2.  Check Account Balance");
        System.out.println("3.  Deposit Funds");
        System.out.println("4.  Withdraw Funds");
        System.out.println("5.  List Previous Transactions");
        System.out.println("6.  Transfer Funds");
        System.out.println("7.  Display Menu");
        System.out.println("9.  Exit");
        System.out.println("Your Option: ");
    }

    public static void displayPrompt() {
        System.out.println("Enter your choice (7 to display options):");
    }




}
