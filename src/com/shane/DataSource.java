package com.shane;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataSource {


    private static DataSource dataSource;

    private Connection connection = null;
    private PreparedStatement queryForAccountInfo;
    private PreparedStatement queryForAccount;
    private PreparedStatement updateAccountRecord;
    private PreparedStatement insertTransaction;
    private PreparedStatement queryTransactions;

    public static final String DB_NAME = "banking";
    public static final String CONNECTION_STRING = "jdbc:mysql://localhost:3306/" + DB_NAME;
    public static final String USER = "default";
    public static final String PASSWORD = "test1234";

    public static final String TABLE_ACCOUNT = "bank_account";
    public static final String COLUMN_PIN = "PIN";
    public static final String COLUMN_ACCOUNT_NUMBER = "account_number";
    public static final String COLUMN_CUSTOMER_NAME = "customer_name";
    public static final String COLUMN_BALANCE = "balance";
    public static final String COLUMN_LAST_UPDATE = "last_update";

    public static final String TABLE_TRANSACTION = "transactions";
    public static final String COLUMN_TRANS_ACCOUNT_NUMBER = "account_number";
    public static final String COLUMN_TRANS_TYPE = "trans_type";
    public static final String COLUMN_TRANS_AMOUNT = "trans_amount";
    public static final String COLUMN_TRANS_XFER_ACCOUNT = "trans_account";
    public static final String COLUMN_TRANS_LAST_UPDATE = "trans_timestamp";


    public static final String QUERY_ACCOUNT_BY_PIN = "SELECT * FROM " + TABLE_ACCOUNT + " WHERE " +
            COLUMN_PIN + " = ?";

    public static final String UPDATE_ACCOUNT_RECORD = "UPDATE " + TABLE_ACCOUNT + " SET " + COLUMN_BALANCE + "=?, " +
            COLUMN_LAST_UPDATE + "=CURRENT_TIMESTAMP WHERE " + COLUMN_ACCOUNT_NUMBER + "=?";

    public static final String INSERT_TRANSACTION = "INSERT INTO " + TABLE_TRANSACTION +
            " VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";

    public static final String GET_TRANSACTIONS = "SELECT * FROM " + TABLE_TRANSACTION + " WHERE " + COLUMN_TRANS_ACCOUNT_NUMBER +
            "= ? ORDER BY " + COLUMN_TRANS_LAST_UPDATE + " DESC";

    public static final String GET_ACCOUNT = "SELECT * FROM " + TABLE_ACCOUNT + " WHERE " + COLUMN_ACCOUNT_NUMBER +
            " = ?";

    private DataSource() {

    }

    public static DataSource getDataSourceInstance() {

        if(dataSource == null) {
            dataSource = new DataSource();
            dataSource.open();
        }

        return dataSource;

    }

    public boolean open() {
        try {
            connection = DriverManager.getConnection(CONNECTION_STRING, USER, PASSWORD);

            return true;
        } catch (SQLException e) {
            System.out.println("Couldn't connect to database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void close() {
        try {
            if(connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Close Failed...");
            e.printStackTrace();
        }
    }

    public BankAccount getAccountInfo(BankAccount account) {


        try {
            queryForAccountInfo = connection.prepareStatement(QUERY_ACCOUNT_BY_PIN);
            queryForAccountInfo.setInt(1, account.getPIN());
            ResultSet results = queryForAccountInfo.executeQuery();


            if(results.next()) {
                account.setAccountNumber(results.getInt(2));
                account.setCustomerName(results.getString(3));
                account.setBalance(results.getDouble(4));

                return account;

            }
        } catch (SQLException e) {
            System.out.println("Query of bank_account table failed...");
            e.printStackTrace();
        } finally {
            try {
                if(queryForAccountInfo != null) {
                    queryForAccountInfo.close();
                }
            } catch(SQLException e) {
                System.out.println("Close failure for queryForAccountInfo");
                System.out.println(e.getMessage());
            }
        }
        return null;
    }

    public BankAccount getAccount(int accountNumber) {

        try {
            queryForAccount = connection.prepareStatement(GET_ACCOUNT);
            queryForAccount.setInt(1, accountNumber);

            System.out.println(queryForAccount);

            queryForAccount.setInt(1, accountNumber);
            ResultSet results = queryForAccount.executeQuery();



            if(results.next()) {
                BankAccount bankAccount = new BankAccount();
                bankAccount.setPIN(results.getInt(1));
                bankAccount.setAccountNumber(results.getInt(2));
                bankAccount.setCustomerName(results.getString(3));
                bankAccount.setBalance(results.getDouble(4));

                return bankAccount;

            } else {
                return null;
            }


        } catch(SQLException e) {
            System.out.println("Query for Account failed...");
            System.out.println(e.getMessage());
        } finally {
            try {
                if(queryForAccount != null) {
                    queryForAccount.close();
                }
            } catch(SQLException e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }
        return null;
    }

    public boolean updateForTransaction(BankAccount bankAccount, Transaction transaction) {

        try {
            connection.setAutoCommit(false);
            if(buildForUpdate(bankAccount, transaction)) {
                connection.commit();
                connection.setAutoCommit(true);
                return true;
            }
        } catch(SQLException e) {
            bankAccount.setStatus("Database Update Error: Please try again later.");
            e.printStackTrace();
            return false;
        }

        return false;

    }

    public boolean transferFunds(BankAccount fromAccount, Transaction fromTransaction,
                                 BankAccount toAccount, Transaction toTransaction, double amount) {
        // THE TRANSFER IS A WITHDRAWAL FROM THE fromAccount AND A DEPOSIT TO THE toAccount

        try {
            connection.setAutoCommit(false);

            if(buildForUpdate(fromAccount, fromTransaction)) {

            } else {
                return false;
            }

            if(buildForUpdate(toAccount, toTransaction)) {

            } else {
                return false;
            }

            connection.commit();

        } catch(SQLException e) {
            e.printStackTrace();
            fromAccount.setStatus("Database Update Error: Please try again later.");
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch(SQLException e) {
                System.out.println("Could not reset Autocommit");
                System.out.println(e.getMessage());
            }
        }



        return true;
    }

    private boolean buildForUpdate(BankAccount bankAccount, Transaction transaction) {

        try {

            updateAccountRecord = connection.prepareStatement(UPDATE_ACCOUNT_RECORD);
            insertTransaction = connection.prepareStatement(INSERT_TRANSACTION);

            updateAccountRecord.setDouble(1, bankAccount.getBalance());
            updateAccountRecord.setInt(2, bankAccount.getAccountNumber());

            insertTransaction.setInt(1, transaction.getAccountNumber());
            insertTransaction.setString(2, "" + transaction.getTransactionType());
            insertTransaction.setDouble(3, transaction.getTransactionAmount());
            insertTransaction.setInt(4, transaction.getTransferAccount());

            if(updateDatabase(updateAccountRecord, TABLE_ACCOUNT)) {

            } else {
                return false;
            }

            if(updateDatabase(insertTransaction, TABLE_TRANSACTION)) {

            } else {
                return false;
            }

            return true;


        } catch (SQLException e) {
            e.printStackTrace();
            e.getMessage();
            e.getSQLState();
            return false;
        } finally {
            try {
                if(updateAccountRecord != null) {
                    updateAccountRecord.close();
                }
                if(insertTransaction != null) {
                    insertTransaction.close();
                }

            } catch (SQLException e) {
                System.out.println("Prepared Statement close() method failed");
                e.printStackTrace();

            }
        }

    }



    private boolean updateDatabase(PreparedStatement preparedStatement, String tableName) {

        try {
            System.out.println(preparedStatement);
            int affectedRows = preparedStatement.executeUpdate();

            if(affectedRows == 1) {
                return true;
            } else {
                connection.rollback();
                System.out.println(preparedStatement);
                throw new SQLException("Update of the " + tableName + " table failed.");
            }

        } catch(SQLException e) {
            System.out.println(preparedStatement);
            e.printStackTrace();
            e.getSQLState();
            try {
                connection.rollback();
            }catch(SQLException e2) {
                System.out.println("Transaction rollback was unsuccessful");
                e2.printStackTrace();
                return false;
            }
            return false;

        }


    }

    public List<Transaction> getTransactions(int accountNumber) {

        List<Transaction> transactions = new ArrayList<>();

        try {
            queryTransactions = connection.prepareStatement(GET_TRANSACTIONS);

            queryTransactions.setInt(1, accountNumber);

            ResultSet results = queryTransactions.executeQuery();

            String transactionTypeString;

            while(results.next()) {
                Transaction transaction = new Transaction(accountNumber);
                transactionTypeString = results.getString(2);
                if (transactionTypeString.length() == 1) {
                    transaction.setTransactionType(transactionTypeString.charAt(0));
                } else {
                    transaction.setTransactionType(' ');
                }

                transaction.setTransactionAmount(results.getDouble(3));
                transaction.setTransferAccount(results.getInt(4));
                transaction.setTimestamp(results.getTimestamp(5));
                transactions.add(transaction);

            }


        } catch(SQLException e) {
            System.out.println("Error occurred while trying to get list of transactions");
            System.out.println(e.getMessage());
            return null;
        } finally {
            try {
                if (queryTransactions != null) {
                    queryTransactions.close();
                }
            } catch(SQLException e2) {
                System.out.println("Prepared statement close failed");
                e2.printStackTrace();
            }
        }
        return transactions;
    }
}
