package com.bank.service;

import com.bank.dao.AccountDAO;
import com.bank.dao.AccountDAOImpl;
import com.bank.dao.TransactionDAO;
import com.bank.dao.TransactionDAOImpl;
import com.bank.exception.BankException;
import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.util.DBUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

/**
 * Business rules and transaction boundaries live here.
 * The DAOs only run SQL; Main only talks to the user.
 */
public class BankService {

    public static final int PAGE_SIZE = 5;
    public static final int STATEMENT_SIZE = 5;

    private final AccountDAO accountDAO = new AccountDAOImpl();
    private final TransactionDAO txnDAO = new TransactionDAOImpl();

    /** A piece of JDBC work that runs on a connection supplied by the service. */
    @FunctionalInterface
    private interface SqlWork<T> {
        T run(Connection con) throws SQLException;
    }

    // ------------------------------------------------------------------ operations

    public int openAccount(String holder, String email, double openingDeposit) {
        if (holder == null || holder.isBlank())
            throw new BankException("Name must not be blank");
        if (email == null || !email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$"))
            throw new BankException("Invalid email format");
        if (openingDeposit < 0)
            throw new BankException("Opening deposit cannot be negative");

        try {
            return inTransaction(con -> {
                int accNo = accountDAO.create(con, new Account(holder.trim(), email.trim(), openingDeposit));
                if (openingDeposit > 0) {
                    txnDAO.insert(con, new Transaction(accNo, Transaction.DEPOSIT,
                            openingDeposit, openingDeposit, null));
                }
                return accNo;
            });
        } catch (BankException e) {
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new BankException("Email already registered: " + email, e);
            }
            throw e;
        }
    }

    public Account getAccount(int accNo) {
        return withConnection(con -> {
            Account acc = accountDAO.findById(con, accNo);
            if (acc == null) throw new BankException("Account " + accNo + " not found");
            return acc;
        });
    }

    public double deposit(int accNo, double amount) {
        requirePositive(amount);
        return inTransaction(con -> {
            if (accountDAO.updateBalance(con, accNo, amount) == 0)
                throw new BankException("Account not found or not active");
            double newBalance = accountDAO.findById(con, accNo).getBalance();
            txnDAO.insert(con, new Transaction(accNo, Transaction.DEPOSIT, amount, newBalance, null));
            return newBalance;
        });
    }

    public double withdraw(int accNo, double amount) {
        requirePositive(amount);
        return inTransaction(con -> {
            if (accountDAO.updateBalance(con, accNo, -amount) == 0)
                throw new BankException("Insufficient funds or account not active");
            double newBalance = accountDAO.findById(con, accNo).getBalance();
            txnDAO.insert(con, new Transaction(accNo, Transaction.WITHDRAW, amount, newBalance, null));
            return newBalance;
        });
    }

    public void transfer(int fromAcc, int toAcc, double amount) {
        requirePositive(amount);
        if (fromAcc == toAcc)
            throw new BankException("Source and target accounts must be different");

        inTransaction(con -> {
            // Always touch the lower account number first, so two opposite transfers
            // (A->B and B->A) lock rows in the same order and cannot deadlock.
            if (fromAcc < toAcc) {
                debit(con, fromAcc, amount);
                credit(con, toAcc, amount);
            } else {
                credit(con, toAcc, amount);
                debit(con, fromAcc, amount);
            }
            double fromBalance = accountDAO.findById(con, fromAcc).getBalance();
            double toBalance = accountDAO.findById(con, toAcc).getBalance();
            txnDAO.insert(con, new Transaction(fromAcc, Transaction.TRANSFER_OUT, amount, fromBalance, toAcc));
            txnDAO.insert(con, new Transaction(toAcc, Transaction.TRANSFER_IN, amount, toBalance, fromAcc));
            return null;
        });
    }

    public List<Transaction> miniStatement(int accNo) {
        return withConnection(con -> {
            if (accountDAO.findById(con, accNo) == null)
                throw new BankException("Account " + accNo + " not found");
            return txnDAO.findRecent(con, accNo, STATEMENT_SIZE);
        });
    }

    public List<Account> listAccounts(int page) {
        if (page < 1) throw new BankException("Page number must be 1 or more");
        return withConnection(con -> accountDAO.findAll(con, page, PAGE_SIZE));
    }

    public void closeAccount(int accNo) {
        inTransaction(con -> {
            Account acc = accountDAO.findById(con, accNo);
            if (acc == null)
                throw new BankException("Account " + accNo + " not found");
            if (!"ACTIVE".equals(acc.getStatus()))
                throw new BankException("Account is already closed");
            if (acc.getBalance() != 0)
                throw new BankException("Balance must be 0 to close the account (current: "
                        + String.format("%.2f", acc.getBalance()) + ")");
            if (accountDAO.close(con, accNo) == 0)
                throw new BankException("Could not close the account");
            return null;
        });
    }

    // ------------------------------------------------------------------ helpers

    private void debit(Connection con, int accNo, double amount) throws SQLException {
        if (accountDAO.updateBalance(con, accNo, -amount) == 0)
            throw new BankException("Insufficient funds or source account not active");
    }

    private void credit(Connection con, int accNo, double amount) throws SQLException {
        if (accountDAO.updateBalance(con, accNo, amount) == 0)
            throw new BankException("Target account not found or not active");
    }

    private void requirePositive(double amount) {
        if (amount <= 0) throw new BankException("Amount must be positive");
    }

    /** Runs work in one transaction: commit on success, roll back on ANY failure. */
    private <T> T inTransaction(SqlWork<T> work) {
        try (Connection con = DBUtil.getConnection()) {
            con.setAutoCommit(false);
            try {
                T result = work.run(con);
                con.commit();                       // only reached when everything worked
                return result;
            } catch (SQLException | RuntimeException e) {
                con.rollback();                     // undo every change made so far
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new BankException("Database error: " + e.getMessage(), e);
        }
    }

    /** Runs read-only work on a short-lived connection (no transaction needed). */
    private <T> T withConnection(SqlWork<T> work) {
        try (Connection con = DBUtil.getConnection()) {
            return work.run(con);
        } catch (SQLException e) {
            throw new BankException("Database error: " + e.getMessage(), e);
        }
    }
}
