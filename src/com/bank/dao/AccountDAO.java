package com.bank.dao;

import com.bank.model.Account;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Every method receives the Connection from the caller, so the service layer
 * decides which calls share one transaction.
 */
public interface AccountDAO {

    /** Inserts the account and returns the generated acc_no. */
    int create(Connection con, Account account) throws SQLException;

    /** Returns the account, or null if it does not exist. */
    Account findById(Connection con, int accNo) throws SQLException;

    /** Page numbers start at 1. */
    List<Account> findAll(Connection con, int page, int pageSize) throws SQLException;

    /**
     * Adds delta (negative to withdraw) to the balance of an ACTIVE account.
     * Returns 0 if the account is missing/closed or the balance would go below zero.
     */
    int updateBalance(Connection con, int accNo, double delta) throws SQLException;

    /** Marks an ACTIVE account with a zero balance as CLOSED. Returns rows changed. */
    int close(Connection con, int accNo) throws SQLException;
}
