package com.bank.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** The only class that knows how to reach the database. */
public final class DBUtil {

    private static final String URL  = env("DB_URL",  "jdbc:mysql://localhost:3306/bankdb");
    private static final String USER = env("DB_USER", "root");
    // Set the DB_PASS environment variable, or replace the default below while practicing.
    private static final String PASS = env("DB_PASS", "change_me");

    private DBUtil() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isEmpty()) ? fallback : value;
    }
}
