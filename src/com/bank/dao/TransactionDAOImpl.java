package com.bank.dao;

import com.bank.model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAOImpl implements TransactionDAO {

    @Override
    public int insert(Connection con, Transaction t) throws SQLException {
        String sql = "INSERT INTO transactions (acc_no, type, amount, balance_after, related_acc) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, t.getAccNo());
            ps.setString(2, t.getType());
            ps.setDouble(3, t.getAmount());
            ps.setDouble(4, t.getBalanceAfter());
            if (t.getRelatedAcc() == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, t.getRelatedAcc());
            }
            return ps.executeUpdate();
        }
    }

    @Override
    public List<Transaction> findRecent(Connection con, int accNo, int limit) throws SQLException {
        String sql = "SELECT txn_id, acc_no, type, amount, balance_after, related_acc, txn_time "
                   + "FROM transactions WHERE acc_no = ? ORDER BY txn_id DESC LIMIT ?";
        List<Transaction> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accNo);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int related = rs.getInt("related_acc");
                    Integer relatedAcc = rs.wasNull() ? null : related;   // NULL-safe read
                    list.add(new Transaction(
                            rs.getInt("txn_id"),
                            rs.getInt("acc_no"),
                            rs.getString("type"),
                            rs.getDouble("amount"),
                            rs.getDouble("balance_after"),
                            relatedAcc,
                            rs.getTimestamp("txn_time")));
                }
            }
        }
        return list;
    }
}
