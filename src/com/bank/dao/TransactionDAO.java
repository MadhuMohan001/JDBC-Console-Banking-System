package com.bank.dao;

import com.bank.model.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface TransactionDAO {

    /** Inserts one history row and returns the number of rows inserted. */
    int insert(Connection con, Transaction txn) throws SQLException;

    /** Most recent transactions of an account, newest first. */
    List<Transaction> findRecent(Connection con, int accNo, int limit) throws SQLException;
}
