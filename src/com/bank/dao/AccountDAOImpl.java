package com.bank.dao;

import com.bank.model.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AccountDAOImpl implements AccountDAO {

    private static final String COLUMNS = "acc_no, holder, email, balance, status";

    @Override
    public int create(Connection con, Account a) throws SQLException {
        String sql = "INSERT INTO accounts (holder, email, balance) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getHolder());
            ps.setString(2, a.getEmail());
            ps.setDouble(3, a.getBalance());
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Account was not created");
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("Database did not return an account number");
            }
        }
    }

    @Override
    public Account findById(Connection con, int accNo) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM accounts WHERE acc_no = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<Account> findAll(Connection con, int page, int pageSize) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM accounts ORDER BY acc_no LIMIT ? OFFSET ?";
        List<Account> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pageSize);
            ps.setInt(2, (page - 1) * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public int updateBalance(Connection con, int accNo, double delta) throws SQLException {
        // The "balance + ? >= 0" condition makes the check and the update one atomic statement.
        String sql = "UPDATE accounts SET balance = balance + ? "
                   + "WHERE acc_no = ? AND status = 'ACTIVE' AND balance + ? >= 0";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, delta);
            ps.setInt(2, accNo);
            ps.setDouble(3, delta);
            return ps.executeUpdate();
        }
    }

    @Override
    public int close(Connection con, int accNo) throws SQLException {
        String sql = "UPDATE accounts SET status = 'CLOSED' "
                   + "WHERE acc_no = ? AND status = 'ACTIVE' AND balance = 0";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accNo);
            return ps.executeUpdate();
        }
    }

    private Account mapRow(ResultSet rs) throws SQLException {
        return new Account(
                rs.getInt("acc_no"),
                rs.getString("holder"),
                rs.getString("email"),
                rs.getDouble("balance"),
                rs.getString("status"));
    }
}
